package com.cs407.savewise.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.savewise.R
//import com.cs407.savewise.viewModels.
import com.cs407.savewise.auth.EmailResult
import com.cs407.savewise.auth.PasswordResult
import com.cs407.savewise.auth.checkEmail
import com.cs407.savewise.auth.checkPassword
import com.cs407.savewise.auth.signIn
import com.cs407.savewise.data.UserState


@Composable
fun LogInSignUpButton(
    email: String,
    password: String,
    //add other parameters if you need
    modifier: Modifier = Modifier,
    onAuthSuccess: (UserState, isNameMissing: Boolean) -> Unit,
    onAuthFailure: (String) -> Unit,
) {
    val context = LocalContext.current

    Button(
        onClick = {
// TODO: 1. Validate email using validateEmail()
// TODO: 2. If email error, update ui with error message
// TODO: 3. Validate password using validatePassword()
// TODO: 4. If password error, update ui with error message
// TODO: 5. If both valid, call signIn()
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

            val passwordResult = checkPassword(password)
            if (passwordResult != PasswordResult.Valid) {
                val errorMessage = when (passwordResult) {
                    PasswordResult.Empty -> context.getString(R.string.empty_password)
                    PasswordResult.Short -> context.getString(R.string.short_password)
                    PasswordResult.Invalid -> context.getString(R.string.invalid_password)
                    else -> "An unknown password error occurred"
                }
                onAuthFailure(errorMessage) // Pass error message back to the parent
                return@Button
            }

            signIn(
                email = email,
                password = password,
                onSuccess = { user, isNameMissing ->
                    onAuthSuccess(user, isNameMissing)
                },
                onFailure = { exception ->
                    onAuthFailure(exception.message ?: "An unknown error occurred")
                }
            )
        },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(50.dp)
    ) {
        Text(stringResource(R.string.login_button))
    }
}

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    //add callback functions or other parameters if you need
    loginButtonClick: (UserState, isNameMissing: Boolean) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error: String? by remember { mutableStateOf(null) }
//TODO Callback for authentication completion

    Scaffold(modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: Add UI components - ErrorText, email field,
// password field, button
            @Composable
            fun ErrorText(error: String?, modifier: Modifier = Modifier) {
                if (error != null)
                    Text(text = error, color = Color.Red, textAlign = TextAlign.Center)
            }

            @Composable
            fun userEmail(
                email: String,
                onEmailChange: (String) -> Unit,
                modifier: Modifier = Modifier
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(id = R.string.Email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = modifier
                )
            }


            @Composable
            fun userPassword(
                password: String,
                onPasswordChange: (String) -> Unit,
                modifier: Modifier = Modifier
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(id = R.string.Password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = modifier
                )
            }

            ErrorText(error = error)

            Spacer(modifier = Modifier.height(16.dp))

            userEmail(
                email = email,
                onEmailChange = { email = it },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            userPassword(
                password = password,
                onPasswordChange = { password = it },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LogInSignUpButton(
                email = email,
                password = password,
                modifier = Modifier.fillMaxWidth(0.8f),
                onAuthSuccess = { user, isNameMissing ->
                    error = null
                    loginButtonClick(user, isNameMissing)
                },
                onAuthFailure = { errorMessage ->
                    error = errorMessage
                }
            )
        }
    }
}