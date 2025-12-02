package com.cs407.savewise.ui

import androidx.compose.foundation.Image
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
import com.cs407.savewise.auth.signIn
import com.cs407.savewise.data.UserState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction


@Composable
fun LogInButton(
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
    loginButtonClick: (UserState, isNameMissing: Boolean) -> Unit,
    onSignUpClicked: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error: String? by remember { mutableStateOf(null) }

    Scaffold(modifier = modifier) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
           /* Image(
                painter = painterResource(id = R.drawable.loginBackground),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )*/

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // App title & tagline
                    Text(
                        text = "SaveWise",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Smart voice-based expense tracker",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    // Error text
                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.Email)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(id = R.string.Password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Login button – reuses your existing auth logic
                    LogInButton(
                        email = email,
                        password = password,
                        modifier = Modifier.fillMaxWidth(),
                        onAuthSuccess = { user, isNameMissing ->
                            error = null
                            loginButtonClick(user, isNameMissing)
                        },
                        onAuthFailure = { errorMessage ->
                            error = errorMessage
                        }
                    )

                    // Sign up row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = onSignUpClicked) {
                            Text("Sign Up")
                        }
                    }
                }
            }
        }
    }
}
