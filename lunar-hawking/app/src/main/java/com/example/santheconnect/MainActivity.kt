package com.example.santheconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.santheconnect.ui.screens.*
import com.example.santheconnect.ui.theme.SantheConnectTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SantheConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SantheApp()
                }
            }
        }
    }
}

@Composable
fun SantheApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val startDest = if (auth.currentUser != null) "home" else "welcome"
    
    NavHost(navController = navController, startDestination = startDest) {
        composable("welcome") { WelcomeScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("forgot_password") { ForgotPasswordScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("calendar") { CalendarScreen(navController) }
        composable("map") { MapScreen(navController) }
        composable("reviews") { ReviewScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}
