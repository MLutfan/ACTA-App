package id.antasari.acta_app.ui.screens

import androidx.compose.material.icons.automirrored.rounded.Chat
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import id.antasari.acta_app.data.AuthRepository
import id.antasari.acta_app.data.ImageRepository
import id.antasari.acta_app.data.dummyActions
import id.antasari.acta_app.ui.components.ActaNotification // Pastikan file ini ada (dari langkah sebelumnya)

// --- DEFINISI MENU BAWAH ---
sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    object Rank : BottomNavItem("rank", "Rank", Icons.Filled.EmojiEvents)
    object Mission : BottomNavItem("mission", "Mission", Icons.Filled.TaskAlt)
    object Feed : BottomNavItem("feed", "Feed", Icons.AutoMirrored.Rounded.Chat)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- SETUP REPOSITORY & FIREBASE ---
    val context = LocalContext.current
    val imageRepo = remember { ImageRepository(context) }
    val authRepo = remember { AuthRepository() }
    val db = FirebaseFirestore.getInstance()

    // --- STATE UNTUK NOTIFIKASI CUSTOM ---
    var notifMessage by remember { mutableStateOf("") }
    var notifVisible by remember { mutableStateOf(false) }
    var notifIsSuccess by remember { mutableStateOf(true) }

    // Helper Function untuk memunculkan notifikasi
    fun showNotif(msg: String, success: Boolean = true) {
        notifMessage = msg
        notifIsSuccess = success
        notifVisible = true
    }

    Scaffold(
        bottomBar = {
            // Sembunyikan BottomBar jika sedang di layar Kamera
            if (currentRoute?.startsWith("camera") != true) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Rank,
                        BottomNavItem.Mission,
                        BottomNavItem.Feed,
                        BottomNavItem.Profile
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }, // Text otomatis pakai Font Poppins dari Theme
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        // BOX UTAMA (Agar notifikasi bisa mengambang di atas konten)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // --- NAV HOST (PENGATUR HALAMAN) ---
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route
            ) {
                // 1. HOME SCREEN
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        onNodeClick = { actionId ->
                            navController.navigate("camera/$actionId")
                        },
                        onNavigateToMission = {
                            navController.navigate(BottomNavItem.Mission.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        // TAMBAHAN: Navigasi ke Shop
                        onNavigateToShop = {
                            navController.navigate("shop")
                        }
                    )
                }

                // 2. CAMERA SCREEN (DYNAMIC ROUTE)
                composable(
                    route = "camera/{actionId}",
                    arguments = listOf(navArgument("actionId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val actionId = backStackEntry.arguments?.getInt("actionId") ?: 0

                    CameraScreen(
                        actionId = actionId,
                        onImageCaptured = { bitmap ->
                            // Tampilkan Notif Loading
                            showNotif("Mengupload bukti...", true)

                            // Proses Upload ke Cloudinary
                            imageRepo.uploadImage(
                                bitmap = bitmap,
                                onSuccess = { imageUrl ->
                                    val currentUser = authRepo.getCurrentUser()

                                    if (currentUser != null) {
                                        val missionData = dummyActions.find { it.id == actionId }
                                        val expReward = missionData?.expReward ?: 0
                                        val seedReward = missionData?.seedReward ?: 0
                                        val missionTitle = missionData?.title ?: "Unknown Action"

                                        val userRef = db.collection("users").document(currentUser.uid)

                                        // 1. AMBIL DATA USER DULU (Untuk dipasang di Post)
                                        userRef.get().addOnSuccessListener { userSnapshot ->
                                            val username = userSnapshot.getString("name") ?: "Eco Warrior"
                                            val avatarId = userSnapshot.getLong("avatarId")?.toInt() ?: 1

                                            // 2. SIMPAN POSTINGAN KE COLLECTION 'posts'
                                            val newPost = hashMapOf(
                                                "userId" to currentUser.uid,
                                                "username" to username,
                                                "userAvatarId" to avatarId,
                                                "imageUrl" to imageUrl,
                                                "actionTitle" to missionTitle,
                                                "timestamp" to FieldValue.serverTimestamp(),
                                                "likes" to 0,
                                                "likedBy" to emptyList<String>() // List UID user yang like
                                            )

                                            db.collection("posts").add(newPost)

                                            // 3. UPDATE XP & SEEDS USER (Logika Lama)
                                            db.runTransaction { transaction ->
                                                val snapshot = transaction.get(userRef)
                                                val currentLevel = snapshot.getLong("currentLevel")?.toInt() ?: 1

                                                transaction.update(userRef, "exp", FieldValue.increment(expReward.toLong()))
                                                transaction.update(userRef, "seeds", FieldValue.increment(seedReward.toLong()))
                                                transaction.update(userRef, "currentStreak", FieldValue.increment(1)) // Tambah streak simulasi

                                                if (actionId == currentLevel) {
                                                    transaction.update(userRef, "currentLevel", currentLevel + 1)
                                                }
                                            }.addOnSuccessListener {
                                                showNotif("Misi Selesai! Postingan diterbitkan.", true)
                                                navController.popBackStack()
                                            }
                                        }
                                    }
                                },
                                onError = { errorMsg ->
                                    showNotif(errorMsg, false)
                                }
                            )
                        },
                        onError = { e ->
                            showNotif("Error Kamera: ${e.message}", false)
                        }
                    )
                }

                // 3. LAYAR LAINNYA (PLACEHOLDER)
                composable(BottomNavItem.Rank.route) {
                    RankScreen()
                }
                composable(BottomNavItem.Mission.route) {
                    MissionsScreen(
                        onNavigateToCamera = {
                            // Logic jika tombol "Go" diklik (opsional, bisa arahkan ke Home atau Camera)
                            navController.navigate(BottomNavItem.Home.route)
                        }
                    )
                }
                composable(BottomNavItem.Feed.route) {
                    FeedScreen()
                }
                composable("edit_profile") {
                    EditProfileScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(BottomNavItem.Profile.route) {
                    ProfileScreen(
                        onLogout = { /* Logic Logout di dalam ProfileScreen */ },
                        onEditProfile = { navController.navigate("edit_profile") },
                        onNavigateToShop = { navController.navigate("shop") } // <-- TAMBAHAN
                    )
                }
                composable("shop") {
                    ShopScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // --- KOMPONEN NOTIFIKASI CUSTOM (OVERLAY) ---
            // Diletakkan paling bawah dalam Box agar muncul di atas layer NavHost
            ActaNotification(
                message = notifMessage,
                isVisible = notifVisible,
                isSuccess = notifIsSuccess,
                onDismiss = { notifVisible = false }
            )
        }
    }
}