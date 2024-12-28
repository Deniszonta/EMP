package com.example.myapplication

import QuoteScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.AccelerometerScreen
import com.example.myapplication.ui.JokeScreen
import com.example.myapplication.ui.LightSensorScreen
import com.example.myapplication.ui.LoginScreen
import com.example.myapplication.ui.RegistrationScreen
import com.example.myapplication.ui.TaskListScreen
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(
                        onNavigateToRegister = { navController.navigate("register") },
                        onLogin = { isSuccess ->
                            if (isSuccess) {
                                navController.navigate("task_list") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable("register") {
                    RegistrationScreen(
                        onRegister = { isSuccess ->
                            if (isSuccess) {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        },
                        onNavigateToLogin = {
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    )
                }
                composable("task_list") {
                    TaskListScreen(navController = navController, onLogout = {
                        navController.navigate("login") {
                            popUpTo("task_list") { inclusive = true }
                        }
                    })
                }
                composable("quote") {
                    QuoteScreen(navController = navController) // Pass NavController here
                }
                composable("joke") {
                    JokeScreen(onBackToHome = { navController.navigate("task_list") })
                }
                composable("light_sensor") {
                    LightSensorScreen(onBackToHome = { navController.navigate("task_list") })
                }
                composable("accelerometer") {
                    AccelerometerScreen(onBackToHome = { navController.navigate("task_list") })
                }
            }
        }
    }
}
