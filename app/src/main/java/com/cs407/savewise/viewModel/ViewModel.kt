package com.cs407.savewise.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.savewise.NoteScreen
import com.cs407.savewise.auth.updateName
import com.cs407.savewise.data.DeleteDao

//import com.cs407.savewise.data.NoteDatabase
import com.cs407.savewise.data.UserState
import com.cs407.savewise.ui.component.Screen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ViewModel(application: Application) : AndroidViewModel(application) {
    // Private mutable state (only ViewModel can modify)
    private val _userState = MutableStateFlow(UserState())

    // Firebase authentication instance
    private val auth: FirebaseAuth = Firebase.auth

    // Public read-only state (UI observes this)
    val userState = _userState.asStateFlow()

    //private val deleteDao: DeleteDao = NoteDatabase.getDatabase(application).deleteDao()

    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo: StateFlow<String?> = _navigateTo.asStateFlow()

    init {
        auth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                setUser(UserState(), false)
                _navigateTo.value = NoteScreen.Login.name

            }
        }
    }

    // Function to update user state
    fun setUser(state: UserState, isNameMissing: Boolean) {
        _userState.value = state
        if (state.uid.isNotBlank()) {
            if (isNameMissing) {
                _navigateTo.value = NoteScreen.AskName.name
            } else {
                _navigateTo.value = Screen.Home.route
            }
        }
    }

    fun onNavigationHandled() {
        _navigateTo.value = null
    }

    fun updateUserProfileName(newName: String) {
        viewModelScope.launch {
            updateName(
                name = newName,
                onSuccess = {
                    _userState.value = _userState.value.copy(name = newName)
                    _navigateTo.value = Screen.Home.route
                },
                onFailure = { exception ->
                    Log.e("UserViewModel", "Failed to update name", exception)
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
        }
    }

    fun deleteAccount() {
        val user = auth.currentUser
        val userId = _userState.value.uid

        if (user != null) {
            viewModelScope.launch {
                try {
                    user.delete().await()
                    Log.i("UserViewModel", "Firebase user deleted successfully.")

                    if (userId.isNotBlank()) {
                        try {
                            TODO( )
                            //deleteDao.delete(userId.hashCode())
                            Log.i("UserViewModel", "Local user data deleted from Room.")
                        } catch (e: Exception) {
                            Log.e("UserViewModel", "Could not delete user from Room DB.", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UserViewModel", "Failed to delete Firebase account.", e)
                }
            }
        }
    }
}