package id.antasari.acta_app.ui.components // Sesuaikan package kamu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import id.antasari.acta_app.R
import id.antasari.acta_app.ui.theme.Poppins // Import font kita tadi
import kotlinx.coroutines.delay

// Warna Notifikasi
val SuccessGreen = Color(0xFF4ADE80)
val ErrorRed = Color(0xFFFF5252)

@Composable
fun ActaNotification(
    message: String,
    isVisible: Boolean,
    isSuccess: Boolean = true,
    onDismiss: () -> Unit
) {
    // Timer otomatis hilang setelah 3 detik
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(3000)
            onDismiss()
        }
    }

    // Animasi Muncul dari Atas
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }), // Masuk dari atas layar
        exit = slideOutVertically(targetOffsetY = { -it }),  // Keluar ke atas layar
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp) // Jarak dari status bar
            .zIndex(99f) // Pastikan selalu di paling depan (atas layer)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.border(
                width = 1.dp,
                color = if (isSuccess) SuccessGreen else ErrorRed, // Border warna status
                shape = RoundedCornerShape(16.dp)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LOGO ACTA DI KIRI
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4), // Hijau sangat muda
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_acta), // Pastikan nama file logo benar
                            contentDescription = "Logo",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // TEKS PESAN
                Column {
                    Text(
                        text = if (isSuccess) "Berhasil!" else "Oops!",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isSuccess) SuccessGreen else ErrorRed
                    )
                    Text(
                        text = message,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}