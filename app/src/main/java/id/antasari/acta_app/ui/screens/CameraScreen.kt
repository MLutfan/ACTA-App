package id.antasari.acta_app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.concurrent.futures.await
import androidx.lifecycle.LifecycleOwner
import id.antasari.acta_app.data.dummyActions
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    actionId: Int,
    onImageCaptured: (Bitmap) -> Unit, // Dipanggil HANYA saat user klik "Kirim"
    onError: (Exception) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // STATE BARU: Menyimpan foto sementara
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Cari data misi untuk HUD
    val mission = remember(actionId) {
        dummyActions.find { it.id == actionId }
    }

    // Permission Logic
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {

            if (capturedBitmap != null) {
                // ==========================
                // TAMPILAN 2: PREVIEW HASIL
                // ==========================

                // 1. Tampilkan Gambar Full Screen
                Image(
                    bitmap = capturedBitmap!!.asImageBitmap(),
                    contentDescription = "Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Agar foto memenuhi layar
                )

                // 2. Tombol Konfirmasi di Bawah
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f)) // Background transparan
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Tombol Ulangi (Kiri)
                    Button(
                        onClick = { capturedBitmap = null }, // Hapus foto, balik ke kamera
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ulangi")
                    }

                    // Tombol Kirim (Kanan)
                    Button(
                        onClick = { onImageCaptured(capturedBitmap!!) }, // Baru upload disini
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF58CC02)), // Warna Hijau Duolingo
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim")
                    }
                }

            } else {
                // ==========================
                // TAMPILAN 1: KAMERA AKTIF
                // ==========================
                CameraViewContent(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    onError = onError,
                    onPhotoTaken = { bitmap ->
                        capturedBitmap = bitmap // Simpan ke state, jangan upload dulu
                    }
                )

                // Overlay HUD Misi (Hanya muncul saat mode kamera)
                if (mission != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(mission.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(mission.description, color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    } else {
        // Tampilan Minta Izin
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("Izinkan Akses Kamera")
            }
        }
    }
}

// Saya pisahkan logika CameraX ke fungsi komponen biar rapi
@Composable
fun CameraViewContent(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onError: (Exception) -> Unit,
    onPhotoTaken: (Bitmap) -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.await()
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
        } catch (e: Exception) { onError(e) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Tombol Shutter (Jepret)
        FloatingActionButton(
            onClick = { takePhoto(context, imageCapture, onPhotoTaken, onError) },
            containerColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(80.dp)
        ) {
            Icon(Icons.Default.Camera, contentDescription = "Jepret", modifier = Modifier.size(32.dp), tint = Color.Black)
        }
    }
}

// --- FUNGSI TAKE PHOTO (HELPER) ---
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (Exception) -> Unit
) {
    val mainExecutor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = image.toBitmap()
            val rotationDegrees = image.imageInfo.rotationDegrees
            val finalBitmap = rotateBitmap(bitmap, rotationDegrees)
            onImageCaptured(finalBitmap)
            image.close()
        }
        override fun onError(exception: ImageCaptureException) { onError(exception) }
    })
}

fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return bitmap
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}