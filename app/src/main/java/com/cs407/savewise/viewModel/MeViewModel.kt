package com.cs407.savewise.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.AndroidViewModel
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
import com.cs407.savewise.data.UserPreferencesRepository


enum class AppThemeMode {
    System,
    Light,
    Dark
}

data class MeUiState(
    val userName: String = "User Name",
    val region: String = "United States",
    val autoRecording: Boolean = false,
    val recordingStorageDays: Int = 7, // 0,1,3,7,30
    val displayName: String = "",
    val profilePictureUri: String? = null,
    val themeMode: AppThemeMode = AppThemeMode.System,
    val autoBackupEnabled: Boolean = false,
    val wifiOnlyBackup: Boolean = true,
)

class MeViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val prefs = UserPreferencesRepository(application.applicationContext)
    private val expenseRepo = com.cs407.savewise.data.ExpenseRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(MeUiState())

    val uiState: StateFlow<MeUiState> = _uiState
    init {
        viewModelScope.launch {
            prefs.preferencesFlow.collect { stored ->
                _uiState.update { state ->
                    state.copy(
                        region = stored.region,
                        autoRecording = stored.autoRecording,
                        recordingStorageDays = stored.recordingStorageDays,
                        themeMode = stored.themeMode,
                        autoBackupEnabled = stored.autoBackupEnabled,
                        wifiOnlyBackup = stored.wifiOnlyBackup
                    )
                }
                expenseRepo.setAutoSyncEnabled(stored.autoBackupEnabled)
            }
        }
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
    fun setRegion(region: String) {
        _uiState.update { it.copy(region = region) }
        viewModelScope.launch { prefs.setRegion(region) }
    }
    fun setAutoRecording(enabled: Boolean) {
        _uiState.update { it.copy(autoRecording = enabled) }
        viewModelScope.launch { prefs.setAutoRecording(enabled) }
    }

    fun setRecordingStorageDays(days: Int) {
        _uiState.update { it.copy(recordingStorageDays = days) }
        viewModelScope.launch { prefs.setRecordingStorageDays(days) }
    }
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
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        _uiState.update { it.copy(autoBackupEnabled = enabled) }
        viewModelScope.launch {
            prefs.setAutoBackupEnabled(enabled)
            expenseRepo.setAutoSyncEnabled(enabled)
        }
    }

    fun setWifiOnlyBackup(enabled: Boolean) {
        _uiState.update { it.copy(wifiOnlyBackup = enabled) }
        viewModelScope.launch { prefs.setWifiOnlyBackup(enabled) }
    }

    fun backupNow() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            expenseRepo.syncNow(uid, force = true)
        }
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
