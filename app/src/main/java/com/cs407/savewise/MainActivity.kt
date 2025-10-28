package com.cs407.savewise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cs407.savewise.ui.theme.SavewiseTheme

import androidx.navigation.compose.*
import com.cs407.savewise.ui.component.BottomNavBar
import com.cs407.savewise.ui.component.Screen
import com.cs407.savewise.ui.screen.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppMain()
        }
    }
}

@Composable
fun AppMain() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Expense.route) { ExpenseScreen() }
            composable(Screen.Me.route) { MeScreen() }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SavewiseTheme {
//        Greeting("Android")
    }
}