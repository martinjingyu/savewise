package com.cs407.savewise.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cs407.savewise.R
import com.cs407.savewise.auth.EmailResult
import com.cs407.savewise.auth.PasswordResult
import com.cs407.savewise.auth.checkEmail
import com.cs407.savewise.auth.checkPassword
import com.cs407.savewise.auth.signUp
import com.cs407.savewise.data.UserState

@Composable
fun SignUpButton(
    email: String,
    password: String,
    modifier: Modifier = Modifier,
    onAuthSuccess: (UserState, isNameMissing: Boolean) -> Unit,
    onAuthFailure: (String) -> Unit,
) {
    val context = LocalContext.current

    Button(
        onClick = {
            // 1. Validate email
            val emailResult = checkEmail(email)
            if (emailResult != EmailResult.Valid) {
                val errorMessage = when (emailResult) {
                    EmailResult.Empty -> context.getString(R.string.empty_email)
                    EmailResult.Invalid -> context.getString(R.string.invalid_email)
                    else -> "An unknown email error occurred"
                }
                onAuthFailure(errorMessage)
                return@Button
            }

            // 2. Validate password
            val passwordResult = checkPassword(password)
            if (passwordResult != PasswordResult.Valid) {
                val errorMessage = when (passwordResult) {
                    PasswordResult.Empty -> context.getString(R.string.empty_password)
                    PasswordResult.Short -> context.getString(R.string.short_password)
                    PasswordResult.Invalid -> context.getString(R.string.invalid_password)
                    else -> "An unknown password error occurred"
                }
                onAuthFailure(errorMessage)
                return@Button
            }

            // 3. Call signUp (this is the key difference from the login page)
            signUp(
                email = email,
                password = password,
                onSuccess = { user, isNameMissing ->
                    onAuthSuccess(user, isNameMissing)
                },
                onFailure = { exception ->
                    onAuthFailure(exception.message ?: "An unknown error occurred during sign up.")
                }
            )
        },
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(50.dp)
    ) {
        Text(stringResource(R.string.signup_button))
    }
}

@Composable
fun SignUpPage(
    modifier: Modifier = Modifier,
    signUpButtonClick: (UserState, isNameMissing: Boolean) -> Unit,
    onLoginClicked: () -> Unit // Callback to navigate back to login
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error: String? by remember { mutableStateOf(null) }

    Scaffold(modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Reusable composables for UI elements
            @Composable
            fun ErrorText(error: String?, modifier: Modifier = Modifier) {
                Text(
                    text = error ?: "",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = modifier.height(20.dp) // Reserve space to prevent layout shifts
                )
            }

            @Composable
            fun UserEmail(email: String, onEmailChange: (String) -> Unit) {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(id = R.string.Email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            @Composable
            fun UserPassword(password: String, onPasswordChange: (String) -> Unit) {
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(id = R.string.Password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            ErrorText(error = error)
            Spacer(modifier = Modifier.height(8.dp))

            UserEmail(email = email) {
                email = it
                error = null // Clear error on new input
            }
            Spacer(modifier = Modifier.height(8.dp))

            UserPassword(password = password) {
                password = it
                error = null // Clear error on new input
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Use the new SignUpButton
            SignUpButton(
                email = email,
                password = password,
                onAuthSuccess = { user, isNameMissing ->
                    error = null
                    signUpButtonClick(user, isNameMissing)
                },
                onAuthFailure = { errorMessage ->
                    error = errorMessage
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation link to go back to the Login screen
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account?")
                TextButton(onClick = onLoginClicked) {
                    Text("Log In")
                }
            }
        }
    }
}
