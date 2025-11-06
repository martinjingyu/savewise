package com.cs407.savewise.auth

import android.util.Log
import com.cs407.savewise.data.UserState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth


/**
 * MILESTONE 2: Authentication Helper
 * Contains all business logic for Firebase authentication
 */

// ============================================
// Email Validation
// ============================================

enum class EmailResult {
    Valid,
    Empty,
    Invalid,
}

fun checkEmail(email: String) : EmailResult{
    if (email.isEmpty()){
        //TODO handle the case when email is empty
        return EmailResult.Empty
    }

    // 1. username of email should only contain "0-9, a-z, _, A-Z, ."
    // 2. there is one and only one "@" between username and server address
    // 3. there are multiple domain names with at least one top-level domain
    // 4. domain name "0-9, a-z, -, A-Z" (could not have "_" but "-" is valid)
    // 5. multiple domain separate with '.'
    // 6. top level domain should only contain letters and at lest 2 letters
    // this email check only valid for this course
    val pattern = Regex("^[\\w.]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$")
    //TODO logic to handle if email matches the pattern or not.
    return if (pattern.matches(email)) {
        EmailResult.Valid
    } else {
        EmailResult.Invalid
    }
}

// ============================================
// Password Validation
// ============================================

enum class PasswordResult {
    Valid,
    Empty,
    Short,
    Invalid
}

fun checkPassword(password: String) : PasswordResult{
    // 1. password should contain at least one uppercase letter, lowercase letter, one digit
    // 2. minimum length: 5
    if (password.isEmpty()) {
        //TODO when password is empty
        return PasswordResult.Empty
    }
    if (password.length < 5) {
        //TODO when password is short
        return PasswordResult.Short
    }
    if (Regex("\\d+").containsMatchIn(password) &&
        Regex("[a-z]+").containsMatchIn(password) &&
        Regex("[A-Z]+").containsMatchIn(password)
    ) {
        //TODO when password is valid
        return PasswordResult.Valid
    } else {
        //TODO when password is invalid
        return PasswordResult.Invalid
    }
}

// ============================================
// Firebase Authentication Functions
// ============================================

/**
 * Sign in existing user with email and password
 * If sign-in fails, automatically attempts to create new account
 */
fun signIn(
    email: String,
    password: String,
    //any other callback function or parameters if you want
    onSuccess: (user: UserState, isNameMissing: Boolean) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d("signIn", "signInWithEmail:success")
                if (user != null) {
                    val displayName = user.displayName
                    val isNameMissing = displayName.isNullOrBlank()

                    // --- CHANGE THIS ---
                    onSuccess(
                        UserState(name = displayName ?: "", uid = user.uid),
                        isNameMissing
                    )
                } else {
                    onFailure(Exception("User object was null after successful sign in."))
                }
// Sign in success
// TODO: Get current user from the response and propogate it
            } else {
// Sign in failed, try creating account
// TODO: Call createAccount function
                Log.w("signIn", "signInWithEmail:failure", task.exception)
                createAccount(email, password, onSuccess, onFailure)
            }
        }
}

/**
 * Create new Firebase account with email and password
 */
fun createAccount(
    email: String,
    password: String,
    //any other callback function or parameters if you want
    onSuccess: (user: UserState, isNameMissing: Boolean) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->
            val user = auth.currentUser
            Log.d("createAccount", "createUserWithEmail:success")
            if (user != null) {
                onSuccess(UserState(name = "", uid = user.uid), true)
            } else {
                onFailure(Exception("User was null after account creation."))
            }
// TODO: Logic to propagate success response
        }
        .addOnFailureListener { exception ->
//TODO error in creation of account
            Log.w("createAccount", "createUserWithEmail:failure", exception)
            onFailure(exception)
        }
}

/**
 * Update Firebase Auth displayName
 * Used in Milestone 3 for username collection
 */
fun updateName(name: String,
               onSuccess: () -> Unit,
               onFailure: (Exception) -> Unit
) {
    //TODO create a request object to update the display name and then call updateProfile() function
    val user = Firebase.auth.currentUser

    if (user == null) {
        onFailure(Exception("No authenticated user found."))
        return
    }

    val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(name)
        .build()

    user.updateProfile(profileUpdates)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 5. If the task is successful, log it and call the onSuccess callback.
                Log.d("AuthHelper", "User profile updated successfully.")
                onSuccess()
            } else {
                // 6. If the task fails, log the error and call the onFailure callback.
                Log.w("AuthHelper", "User profile update failed.", task.exception)
                onFailure(task.exception ?: Exception("Unknown error updating profile."))
            }
        }
}