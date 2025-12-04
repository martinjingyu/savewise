package com.cs407.savewise.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cs407.savewise.data.UserState
import androidx.compose.material3.Checkbox
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import com.cs407.savewise.auth.LoginPrefs
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Checkbox

@Composable
fun AuthScreen(
    loginButtonClick: (UserState, Boolean) -> Unit,
    signUpButtonClick: (UserState, Boolean) -> Unit,
) {
    // true = login side, false = signup side
    var isLogin by rememberSaveable { mutableStateOf(true) }

    // rotation from 0f (login) to 180f (signup)
    val cardRotationY by animateFloatAsState(
        targetValue = if (isLogin) 0f else 180f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "cardRotationY"
    )

    val cameraDistance = with(LocalDensity.current) { 16.dp.toPx() }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // LoginBackground()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer box rotates
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 360.dp)
                        .graphicsLayer {
                            this.rotationY = cardRotationY
                            this.cameraDistance = cameraDistance
                        }
                ) {
                    if (cardRotationY <= 90f) {
                        // FRONT: Login
                        AuthLoginCard(
                            onSwitchToSignUp = { isLogin = false },
                            loginButtonClick = loginButtonClick
                        )
                    } else {
                        // BACK: SignUp – rotate inner content back so text isn't mirrored
                        Box(
                            modifier = Modifier.graphicsLayer {
                                rotationY = 180f
                            }
                        ) {
                            AuthSignUpCard(
                                onSwitchToLogin = { isLogin = true },
                                signUpButtonClick = signUpButtonClick
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun AuthLoginCard(
    onSwitchToSignUp: () -> Unit,
    loginButtonClick: (UserState, Boolean) -> Unit,
) {
    val context = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    var rememberPassword by rememberSaveable { mutableStateOf(false) }
    var loadedFromPrefs by remember { mutableStateOf(false) }

    // Load saved email/password when the composable first shows
    LaunchedEffect(Unit) {
        val saved = LoginPrefs.load(context)
        email = saved.email
        password = saved.password
        rememberPassword = saved.rememberPassword
        loadedFromPrefs = true
    }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SaveWise",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Smart voice-based expense tracker",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {


                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberPassword,
                        onCheckedChange = { rememberPassword = it }
                    )
                    Text("Remember password")
                }
            }

            Spacer(Modifier.height(8.dp))

            LogInButton(
                email = email,
                password = password,
                modifier = Modifier.fillMaxWidth(),
                onAuthSuccess = { user, isNameMissing ->
                    error = null
                    // Save or clear remembered values based on the checkboxes
                    LoginPrefs.save(
                        context = context,
                        email = email,
                        password = password,
                        rememberPassword = rememberPassword
                    )
                    loginButtonClick(user, isNameMissing)
                },
                onAuthFailure = { msg ->
                    error = msg
                }
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onSwitchToSignUp) {
                    Text("Sign Up")
                }
            }
        }
    }
}


@Composable
private fun AuthSignUpCard(
    onSwitchToLogin: () -> Unit,
    signUpButtonClick: (UserState, Boolean) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create account",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sign up to start tracking your spending.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )



            Spacer(Modifier.height(8.dp))

            SignUpButton(
                email = email,
                password = password,
                modifier = Modifier.fillMaxWidth(),
                onAuthSuccess = { user, isNameMissing ->
                    error = null
                    signUpButtonClick(user, isNameMissing)
                },
                onAuthFailure = { msg ->
                    error = msg
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onSwitchToLogin) {
                    Text("Log In")
                }
            }
        }
    }
}
