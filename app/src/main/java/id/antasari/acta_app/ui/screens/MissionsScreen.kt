package id.antasari.acta_app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import id.antasari.acta_app.data.AuthRepository
import id.antasari.acta_app.ui.theme.Poppins

// Warna UI
val PrimaryGreen = Color(0xFF4ADE80)
val BgColor = Color(0xFFF5F7FA)

data class MissionItem(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val rewardSeeds: Int,
    val icon: ImageVector
)

@Composable
fun MissionsScreen(
    onNavigateToCamera: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val authRepo = AuthRepository()

    // STATE DATA USER
    var seeds by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var currentLevel by remember { mutableIntStateOf(1) }

    // STATE CLAIMED MISSIONS (List ID misi yang sudah diklaim)
    // Kita simpan list ID di sini, bukan di dalam kartu
    var claimedMissions by remember { mutableStateOf<List<String>>(emptyList()) }

    // TAB STATE
    var selectedTab by remember { mutableIntStateOf(0) }

    // Ambil Data Live dari Firebase
    DisposableEffect(Unit) {
        val user = authRepo.getCurrentUser()
        if (user != null) {
            val listener = db.collection("users").document(user.uid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        seeds = doc.getLong("seeds")?.toInt() ?: 0
                        streak = doc.getLong("currentStreak")?.toInt() ?: 0
                        currentLevel = doc.getLong("currentLevel")?.toInt() ?: 1

                        // AMBIL DATA KLAIM DARI FIREBASE
                        // Pastikan di database fieldnya array string
                        val claimedList = doc.get("claimed_missions") as? List<String>
                        claimedMissions = claimedList ?: emptyList()
                    }
                }
            onDispose { listener.remove() }
        } else onDispose { }
    }

    val dailyMissions = listOf(
        MissionItem(
            id = "daily_login",
            title = "Daily Login",
            description = "Buka aplikasi ACTA hari ini.",
            currentProgress = 1,
            maxProgress = 1,
            rewardSeeds = 5,
            icon = Icons.Rounded.CheckCircle
        ),
        MissionItem(
            id = "act_now",
            title = "Selesaikan Aksi",
            description = "Selesaikan level/aksi kamu saat ini.",
            currentProgress = if (currentLevel > 1) 1 else 0,
            maxProgress = 1,
            rewardSeeds = 20,
            icon = Icons.Rounded.EnergySavingsLeaf
        )
    )

    val weeklyMissions = listOf(
        MissionItem(
            id = "streak_3",
            title = "Streak Master",
            description = "Capai 3 hari streak berturut-turut.",
            currentProgress = streak,
            maxProgress = 3,
            rewardSeeds = 50,
            icon = Icons.Rounded.EnergySavingsLeaf
        ),
        MissionItem(
            id = "level_5",
            title = "Eco Warrior",
            description = "Capai Level 5 dalam perjalananmu.",
            currentProgress = currentLevel,
            maxProgress = 5,
            rewardSeeds = 100,
            icon = Icons.Rounded.CheckCircle
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().background(BgColor).padding(20.dp)
    ) {
        Text("Missions", fontFamily = Poppins, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = Color(0xFF1E293B))
        Text("Selesaikan tugas & panen seeds!", fontFamily = Poppins, fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(4.dp)) {
            TabButton(text = "Daily", isSelected = selectedTab == 0, onClick = { selectedTab = 0 }, modifier = Modifier.weight(1f))
            TabButton(text = "Weekly", isSelected = selectedTab == 1, onClick = { selectedTab = 1 }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val missionsToShow = if (selectedTab == 0) dailyMissions else weeklyMissions

            items(
                items = missionsToShow,
                key = { it.id } // <--- INI KUNCINYA! Agar Compose tidak bingung saat ganti tab
            ) { mission ->

                // Cek apakah ID misi ini ada di list yang sudah diklaim
                val isAlreadyClaimed = claimedMissions.contains(mission.id)

                MissionCard(
                    mission = mission,
                    isClaimed = isAlreadyClaimed, // Status dikirim dari luar
                    onClaim = {
                        val user = authRepo.getCurrentUser()
                        if (user != null) {
                            // UPDATE FIREBASE: Tambah Seeds & Masukkan ID ke list claimed
                            val userRef = db.collection("users").document(user.uid)

                            db.runTransaction { transaction ->
                                transaction.update(userRef, "seeds", FieldValue.increment(mission.rewardSeeds.toLong()))
                                // Simpan ID misi agar tidak bisa diklaim lagi
                                transaction.update(userRef, "claimed_missions", FieldValue.arrayUnion(mission.id))
                            }.addOnSuccessListener {
                                Toast.makeText(context, "Klaim Sukses!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

// --- KOMPONEN KARTU MISI (STATELESS) ---
// Hapus semua 'remember' state dari dalam sini
@Composable
fun MissionCard(
    mission: MissionItem,
    isClaimed: Boolean, // <--- Terima status dari parent
    onClaim: () -> Unit
) {
    val progress = (mission.currentProgress.toFloat() / mission.maxProgress.toFloat()).coerceIn(0f, 1f)
    val isCompleted = mission.currentProgress >= mission.maxProgress

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(if (isCompleted) PrimaryGreen.copy(alpha=0.1f) else Color.Gray.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(imageVector = mission.icon, contentDescription = null, tint = if (isCompleted) PrimaryGreen else Color.Gray)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = mission.title, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = mission.description, fontFamily = Poppins, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF9C4), border = BorderStroke(1.dp, Color(0xFFFFD54F))) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.EnergySavingsLeaf, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Text("+${mission.rewardSeeds}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "${mission.currentProgress}/${mission.maxProgress}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(text = "${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(isCompleted) PrimaryGreen else Color.Gray)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = PrimaryGreen, trackColor = Color(0xFFEDF2F7))

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Action
            Button(
                onClick = onClaim,
                modifier = Modifier.fillMaxWidth().height(45.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isCompleted && !isClaimed, // Nonaktif jika sudah diklaim
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    disabledContainerColor = if (isClaimed) Color.Gray else Color(0xFFE2E8F0),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = if (isClaimed) "CLAIMED" else if (isCompleted) "CLAIM REWARD" else "IN PROGRESS",
                    fontWeight = FontWeight.Bold,
                    fontFamily = Poppins
                )
            }
        }
    }
}

// (Helper TabButton SAMA PERSIS dengan sebelumnya)
@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(40.dp).clip(RoundedCornerShape(10.dp)).background(if (isSelected) PrimaryGreen else Color.Transparent).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(text = text, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Gray, fontFamily = Poppins)
    }
}