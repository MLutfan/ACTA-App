package id.antasari.acta_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.acta_app.R
import id.antasari.acta_app.data.AuthRepository
import id.antasari.acta_app.ui.theme.Poppins
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val authRepo = AuthRepository()

    // Logika Pengecekan Login
    LaunchedEffect(Unit) {
        delay(2500) // Tahan 2.5 detik biar logo kelihatan (Branding)

        if (authRepo.getCurrentUser() != null) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    // UI Splash
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Background Putih Bersih
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_acta),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nama App
            Text(
                text = "ACTA",
                fontFamily = Poppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = Color(0xFF1E293B) // TextDark
            )

            // Slogan
            Text(
                text = "Every Action Matters",
                fontFamily = Poppins,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        // Loading kecil di bawah
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .size(24.dp),
            color = Color(0xFF4ADE80) // NeonGreen
        )
    }
}