package com.cs407.savewise

import android.os.Bundle

import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cs407.savewise.ui.AskNamePage
import com.cs407.savewise.ui.LoginPage
import com.cs407.savewise.ui.component.BottomNavBar
import com.cs407.savewise.ui.component.Screen
import com.cs407.savewise.ui.screen.ExpenseScreen
import com.cs407.savewise.ui.screen.HomeScreen
import com.cs407.savewise.ui.screen.MeScreen
import com.cs407.savewise.ui.theme.SavewiseTheme
import com.cs407.savewise.viewModel.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.cs407.savewise.service.SpeechRecognizerHelper
import com.cs407.savewise.ui.AskNamePage
import com.cs407.savewise.viewModel.HomeViewModel
import androidx.fragment.app.FragmentActivity
import com.cs407.savewise.service.WavAudioRecorder
import com.cs407.savewise.service.WhisperApi
import com.cs407.savewise.ui.SignUpPage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.foundation.isSystemInDarkTheme
import com.cs407.savewise.ui.theme.SavewiseTheme
import com.cs407.savewise.viewModel.MeViewModel
import com.cs407.savewise.viewModel.AppThemeMode


class MainActivity : FragmentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        setContent {
            val homeViewModel: HomeViewModel = viewModel()
            val meViewModel: MeViewModel = viewModel()

            val meState by meViewModel.uiState.collectAsState()
            val darkTheme = when (meState.themeMode) {
                AppThemeMode.System -> isSystemInDarkTheme()
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
            }

            SavewiseTheme(darkTheme = darkTheme) {
                val speechHelper = remember {
                    SpeechRecognizerHelper(
                        context = this,
                        onResult = { homeViewModel.onSpeechResult(it) },
                        onError = { homeViewModel.onSpeechError(it) }
                    )
                }
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val audioFile = File(context.cacheDir, "speech.wav")

                val recorder = remember {
                    WavAudioRecorder(
                        context = context,
                        file = audioFile,
                        onError = { msg ->
                            println("❌ Recorder error: $msg")
                            homeViewModel.onSpeechError(msg)
                        }
                    )
                }

                val onStartSpeech = remember {
                    {
                        homeViewModel.onSpeechStart()
                        recorder.start()
                    }
                }

                val onStopSpeech = remember {
                    {
                        recorder.stop()
                        homeViewModel.onSpeechStop()
                        scope.launch {
                            try {
                                val text = WhisperApi.transcribe(audioFile)
                                homeViewModel.onSpeechResult(text)
                            } catch (e: Exception) {
                                homeViewModel.onSpeechError(e.message ?: "error")
                            }
                        }
                        Unit
                    }
                }

                AppMain(
                    homeViewModel = homeViewModel,
                    meViewModel = meViewModel,
                    onStartSpeech = onStartSpeech,
                    onStopSpeech = onStopSpeech,
                )
            }
        }

    }
}

@Composable
fun AppMain(
    viewModel: ViewModel = viewModel(),
    homeViewModel: HomeViewModel,
    meViewModel: MeViewModel,
    onStartSpeech: () -> Unit,
    onStopSpeech: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    //val navController = rememberNavController()

    val userState by viewModel.userState.collectAsState()
    val navigateTo by viewModel.navigateTo.collectAsState()

    LaunchedEffect(navigateTo) {
        navigateTo?.let { route ->
            navController.navigate(route) {
                // Always clear the back stack up to Login when navigating away from it
                if (route == Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
                launchSingleTop = true // Avoid creating multiple instances of the same screen
            }
            viewModel.onNavigationHandled()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Expense.route,
        Screen.Me.route
    )

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NoteScreen.Login.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = NoteScreen.Login.name) {
                LoginPage(
                    loginButtonClick = { user, isNameMissing  ->
                        viewModel.setUser(user, isNameMissing)
                    },
                    onSignUpClicked = {
                        navController.navigate(NoteScreen.SignUp.name)
                    }
                )
            }
            composable(route = NoteScreen.SignUp.name) {
                SignUpPage(
                    signUpButtonClick = { user, isNameMissing ->
                        viewModel.setUser(user, isNameMissing)
                    },
                    // This allows the user to navigate back to the login screen
                    onLoginClicked = {
                        navController.popBackStack() // Go back to the previous screen (LoginPage)
                    }
                )
            }
            composable(route = NoteScreen.AskName.name) {
                AskNamePage(
                    onConfirmClick = { newName ->
                        viewModel.updateUserProfileName(newName)
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onStartSpeech = onStartSpeech,
                    onStopSpeech = onStopSpeech,
                    onSettingClick = {
                        navController.navigate(Screen.Me.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Expense.route) { ExpenseScreen() }
            composable(Screen.Me.route) {
                MeScreen(
                    vm = meViewModel,
                    onLogout = {
                        navController.navigate(NoteScreen.Login.name) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onDeleteAccount = {
                        // Go back to Login and clear the whole stack
                        navController.navigate(NoteScreen.Login.name) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }


        }
    }
}

enum class NoteScreen(@param:StringRes val title: Int) {
    Login(title = R.string.login_screen),
    SignUp(title = R.string.signup_screen),
    NoteList(title = R.string.note_list_screen),
    AskName(title = R.string.name_hint)
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SavewiseTheme {
//        Greeting("Android")
    }
}