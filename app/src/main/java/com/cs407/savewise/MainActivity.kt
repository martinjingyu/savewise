package com.cs407.savewise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cs407.savewise.ui.theme.SavewiseTheme

import androidx.navigation.compose.*
import com.cs407.savewise.ui.LoginPage
import com.cs407.savewise.ui.component.BottomNavBar
import com.cs407.savewise.ui.component.Screen
import com.cs407.savewise.ui.screen.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.cs407.savewise.R
import com.cs407.savewise.ui.AskNamePage
import com.cs407.savewise.viewModel.ViewModel



class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContent {
            AppMain()
        }
    }
}

@Composable
fun AppMain(
    viewModel: ViewModel = viewModel(),
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
                HomeScreen(onSettingClick = {
                    navController.navigate(Screen.Me.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }



            composable(Screen.Expense.route) { ExpenseScreen() }
            composable(Screen.Me.route) { MeScreen() }
        }
    }
}

enum class NoteScreen(@param:StringRes val title: Int) {
    Login(title = R.string.login_screen),
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