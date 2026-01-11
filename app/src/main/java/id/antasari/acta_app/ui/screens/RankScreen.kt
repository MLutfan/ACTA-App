package id.antasari.acta_app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import id.antasari.acta_app.data.AuthRepository
import id.antasari.acta_app.ui.theme.Poppins

// Warna Khusus Rank
val GoldColor = Color(0xFFFFD700)
val SilverColor = Color(0xFFC0C0C0)
val BronzeColor = Color(0xFFCD7F32)
// Note: NeonGreen tidak didefinisikan lagi disini karena sudah ada di file lain

// Data Model User untuk Leaderboard
data class LeaderboardUser(
    val uid: String,
    val name: String,
    val exp: Int,
    val rank: Int = 0
)

@Composable
fun RankScreen() {
    val db = FirebaseFirestore.getInstance()
    val authRepo = AuthRepository()
    val currentUser = authRepo.getCurrentUser()

    // State Data Leaderboard
    var leaderboardList by remember { mutableStateOf<List<LeaderboardUser>>(emptyList()) }
    var myData by remember { mutableStateOf<LeaderboardUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Logic: Ambil 50 User dengan EXP Tertinggi (Real-time)
    DisposableEffect(Unit) {
        val query = db.collection("users")
            .orderBy("exp", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener

            if (snapshot != null) {
                val list = snapshot.documents.mapIndexed { index, doc ->
                    LeaderboardUser(
                        uid = doc.id,
                        name = doc.getString("name") ?: "Unknown Warrior",
                        exp = doc.getLong("exp")?.toInt() ?: 0,
                        rank = index + 1
                    )
                }
                leaderboardList = list

                // Cari data diri sendiri di list
                if (currentUser != null) {
                    myData = list.find { it.uid == currentUser.uid }
                }
                isLoading = false
            }
        }
        onDispose { listener.remove() }
    }

    // UI LAYOUT
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // 1. HEADER & LEAGUE CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .padding(24.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Leaderboard",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tampilkan Liga User Saat Ini
            if (myData != null) {
                LeagueBadge(exp = myData!!.exp)
            } else if (!isLoading) {
                // Fallback kalau user baru banget dan belum masuk top 50
                LeagueBadge(exp = 0)
            }
        }

        // 2. PODIUM & LIST
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF4ADE80) // Pakai hex manual biar aman
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // A. PODIUM (TOP 3)
                    item {
                        if (leaderboardList.isNotEmpty()) {
                            PodiumSection(leaderboardList)
                        }
                    }

                    // B. LIST SISANYA (RANK 4 KE BAWAH)
                    val remainingList = if (leaderboardList.size > 3) leaderboardList.subList(3, leaderboardList.size) else emptyList()

                    itemsIndexed(remainingList) { _, user ->
                        RankListItem(user = user, isMe = user.uid == currentUser?.uid)
                    }
                }
            }
        }
    }
}

// --- KOMPONEN PODIUM (JUARA 1, 2, 3) ---
@Composable
fun PodiumSection(users: List<LeaderboardUser>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom // Ratakan bawah agar efek podium terlihat
    ) {
        // JUARA 2 (KIRI)
        if (users.size >= 2) {
            PodiumItem(user = users[1], place = 2, color = SilverColor, height = 120, modifier = Modifier.weight(1f))
        }

        // JUARA 1 (TENGAH - TERBESAR)
        if (users.isNotEmpty()) {
            PodiumItem(user = users[0], place = 1, color = GoldColor, height = 150, modifier = Modifier.weight(1.2f).zIndex(1f))
        }

        // JUARA 3 (KANAN)
        if (users.size >= 3) {
            PodiumItem(user = users[2], place = 3, color = BronzeColor, height = 100, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PodiumItem(user: LeaderboardUser, place: Int, color: Color, height: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(if (place == 1) 70.dp else 55.dp)
                .border(3.dp, color, CircleShape)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Person, null, tint = Color.Gray)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nama
        Text(
            text = user.name,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color(0xFF1E293B)
        )
        Text(
            text = "${user.exp} XP",
            fontFamily = Poppins,
            fontSize = 10.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Kotak Podium
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.8f), color)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "#$place",
                fontFamily = Poppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

// --- KOMPONEN LIST ITEM (RANK 4++) ---
@Composable
fun RankListItem(user: LeaderboardUser, isMe: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) Color(0xFFF0FDF4) else Color.White // Hijau muda kalau itu kita
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isMe) BorderStroke(1.dp, Color(0xFF4ADE80)) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = "${user.rank}",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.width(30.dp)
            )

            // Avatar Kecil
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Person, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nama & Liga
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if(isMe) "${user.name} (You)" else user.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if(isMe) Color(0xFF2E8B57) else Color(0xFF1E293B)
                )
                Text(
                    text = getTierName(user.exp), // Helper function nama tier
                    fontFamily = Poppins,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            // Total XP
            Surface(
                color = Color(0xFFFFF9C4),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${user.exp}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFF57F17)
                    )
                }
            }
        }
    }
}

// --- KOMPONEN BADGE LIGA (DI HEADER) ---
@Composable
fun LeagueBadge(exp: Int) {
    val tierName = getTierName(exp)
    val nextTierExp = getNextTierExp(exp)
    val progress = if (nextTierExp > 0) (exp.toFloat() / nextTierExp.toFloat()) else 1f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Rounded.EmojiEvents,
            contentDescription = null,
            tint = GoldColor,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = tierName, // Nama Tier Eco
            fontFamily = Poppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = Color.White
        )
        Text(
            text = "$exp / $nextTierExp XP to next tier",
            fontFamily = Poppins,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Bar Tier
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .width(200.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF4ADE80), // NeonGreen manual biar aman
            trackColor = Color.White.copy(alpha = 0.2f),
        )
    }
}

// --- HELPER LOGIC TIER ---
// Pastikan fungsi ini ada di level paling bawah file (di luar kelas apapun)

fun getTierName(exp: Int): String {
    return when (exp) {
        in 0..200 -> "Seedling 🌱"       // Bibit
        in 201..500 -> "Sprout 🌿"       // Tunas
        in 501..1000 -> "Sapling 🌳"     // Tanaman Muda
        in 1001..2500 -> "Forest 🌲"     // Hutan
        else -> "Guardian 🌍"            // Penjaga Bumi (Top Tier)
    }
}

fun getNextTierExp(exp: Int): Int {
    return when (exp) {
        in 0..200 -> 200
        in 201..500 -> 500
        in 501..1000 -> 1000
        in 1001..2500 -> 2500
        else -> 5000 // Max Target
    }
}