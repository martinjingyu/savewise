package com.cs407.savewise.viewModel

import android.net.Uri
import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


enum class AppThemeMode {
    System,
    Light,
    Dark
}

data class MeUiState(
    val userName: String = "User Name",
    val region: String = "United States",
    val autoRecording: Boolean = false,
    val language: String = "English",
    val recordingStorageDays: Int = 7, // 0,1,3,7,30
    val displayName: String = "",
    val profilePictureUri: String? = null,
    val themeMode: AppThemeMode = AppThemeMode.System,
)

class MeViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(MeUiState())

    val uiState: StateFlow<MeUiState> = _uiState
    init {
        refreshDisplayNameFromFirebase()
    }

    fun refreshDisplayNameFromFirebase() {
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName
        _uiState.update { state ->
            state.copy(displayName = name?.takeIf { it.isNotBlank() } ?: "User Name")
        }
    }

    fun setUserName(name: String) = _uiState.update { it.copy(userName = name) }
    fun setRegion(region: String) = _uiState.update { it.copy(region = region) }
    fun setAutoRecording(enabled: Boolean) = _uiState.update { it.copy(autoRecording = enabled) }
    fun setLanguage(lang: String) = _uiState.update { it.copy(language = lang) }
    fun setRecordingStorageDays(days: Int) = _uiState.update { it.copy(recordingStorageDays = days) }
    fun updateProfilePicture(uri: Uri) {
        _uiState.update { it.copy(profilePictureUri = uri.toString()) }
    }

    fun clearProfilePicture() {
        _uiState.update { it.copy(profilePictureUri = null) }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (success: Boolean, message: String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "No logged-in user.")
            return
        }

        val email = user.email
        if (email.isNullOrEmpty()) {
            onResult(false, "Current user has no email.")
            return
        }

        // 1) Re-authenticate with current password
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (!reauthTask.isSuccessful) {
                    onResult(false, reauthTask.exception?.localizedMessage ?: "Re-authentication failed.")
                    return@addOnCompleteListener
                }

                // 2) Actually update password
                user.updatePassword(newPassword)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            onResult(true, null)
                        } else {
                            onResult(false, updateTask.exception?.localizedMessage ?: "Failed to update password.")
                        }
                    }
            }
    }

    fun sendPasswordResetEmailForCurrentUser(
        onResult: (success: Boolean, message: String?) -> Unit
    ) {
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            onResult(false, "No email associated with this account.")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                }
            }
    }

    fun updateDisplayName(
        newName: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) {
            onResult(false, "Name cannot be empty.")
            return
        }

        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(displayName = trimmed) }
            onResult(false, "No logged-in user.")
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(trimmed)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update local UI state to match Firebase
                    _uiState.update { it.copy(displayName = trimmed) }
                    onResult(true, null)
                } else {
                    onResult(
                        false,
                        task.exception?.localizedMessage ?: "Failed to update display name."
                    )
                }
            }
    }


    fun updateThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun logout() {
        // Sign out from Firebase
        auth.signOut()

        //reset Me screen UI state to defaults
        _uiState.value = MeUiState()
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                Firebase.auth.currentUser?.delete()?.await()
                _uiState.value = MeUiState() // Clear UI state on deletion
            } catch (e: Exception) {
                // Handle exception, e.g., show an error message
                Log.e("MeViewModel", "Error deleting account", e)
            }
        }
    }
}
