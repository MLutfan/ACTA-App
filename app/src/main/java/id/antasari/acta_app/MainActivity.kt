package id.antasari.acta_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import id.antasari.acta_app.ui.screens.LoginScreen
import id.antasari.acta_app.ui.screens.MainScreen
import id.antasari.acta_app.ui.screens.RegisterScreen
import id.antasari.acta_app.ui.screens.SplashScreen
import id.antasari.acta_app.ui.theme.ACTAAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ACTAAppTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    NavHost(navController = navController, startDestination = "splash") {

        // 1. SPLASH SCREEN
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                },
                onNavigateToHome = {
                    navController.navigate("main") { popUpTo("splash") { inclusive = true } }
                }
            )
        }

        // 2. LOGIN SCREEN
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") { popUpTo("login") { inclusive = true } }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 3. REGISTER SCREEN
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("main") { popUpTo("register") { inclusive = true } }
                },
                onNavigateToLogin = {
                    navController.popBackStack() // Kembali ke Login
                }
            )
        }

        // 4. MAIN SCREEN (Di dalam sini ada navigasi Home/Camera sendiri)
        composable("main") {
            MainScreen()
        }
    }
}