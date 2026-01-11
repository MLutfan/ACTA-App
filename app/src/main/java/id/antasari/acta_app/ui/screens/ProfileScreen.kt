package id.antasari.acta_app.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.antasari.acta_app.MainActivity
import id.antasari.acta_app.data.AuthRepository
import id.antasari.acta_app.ui.theme.Poppins
import id.antasari.acta_app.utils.getAvatarById

data class Achievement(val title: String, val desc: String, val icon: ImageVector, val isUnlocked: Boolean)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onNavigateToShop: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val authRepo = AuthRepository()
    val context = LocalContext.current

    // STATE DATA USER
    var name by remember { mutableStateOf("Loading...") }
    var exp by remember { mutableIntStateOf(0) }
    var seeds by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var streak by remember { mutableIntStateOf(0) }
    var avatarId by remember { mutableIntStateOf(1) }

    DisposableEffect(Unit) {
        val user = authRepo.getCurrentUser()
        if (user != null) {
            val listener = db.collection("users").document(user.uid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        name = doc.getString("name") ?: "Eco Warrior"
                        exp = doc.getLong("exp")?.toInt() ?: 0
                        seeds = doc.getLong("seeds")?.toInt() ?: 0
                        level = doc.getLong("currentLevel")?.toInt() ?: 1
                        streak = doc.getLong("currentStreak")?.toInt() ?: 0
                        avatarId = doc.getLong("avatarId")?.toInt() ?: 1
                    }
                }
            onDispose { listener.remove() }
        } else onDispose { }
    }

    val achievements = listOf(
        Achievement("Newbie", "Login pertama kali", Icons.Rounded.EmojiPeople, true),
        Achievement("Seedling", "Capai 100 XP", Icons.Rounded.Spa, exp >= 100),
        Achievement("Rich Kid", "Kumpulkan 500 Seeds", Icons.Rounded.MonetizationOn, seeds >= 500),
        Achievement("Streak Master", "Capai 3 Hari Streak", Icons.Rounded.LocalFireDepartment, streak >= 3),
        Achievement("Eco Hero", "Capai 1000 XP", Icons.Rounded.Public, exp >= 1000)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
    ) {
        // 1. HEADER PROFIL
        val currentAvatar = getAvatarById(avatarId)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF4ADE80), RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // AVATAR
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable { onEditProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = currentAvatar.icon, contentDescription = null, tint = currentAvatar.color, modifier = Modifier.size(60.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = name, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)

                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(top = 8.dp)) {
                    // getTierName diambil dari RankScreen.kt (karena satu package)
                    Text(
                        text = getTierName(exp),
                        fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 2. KARTU STATISTIK
        Column(modifier = Modifier.offset(y = (-50).dp).padding(horizontal = 20.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ProfileStatItem(Icons.Rounded.Star, "$exp", "Total XP", Color(0xFFFBC02D))
                    ProfileStatItem(Icons.Rounded.EnergySavingsLeaf, "$seeds", "Seeds", Color(0xFF4ADE80))
                    ProfileStatItem(Icons.Rounded.LocalFireDepartment, "$streak", "Streak", Color(0xFFFF5722))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. ACHIEVEMENTS
            SectionHeader(title = "Achievements")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(achievements) { item -> AchievementItem(item) }
            }

            // 4. COMMUNITY
            SectionHeader(title = "Eco Community")
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.weight(1f)) {
                        for(i in 1..4) {
                            Box(modifier = Modifier.size(36.dp).offset(x = (i * -10).dp).clip(CircleShape).background(Color.LightGray).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Person, null, tint = Color.White) }
                        }
                    }
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E8B57)), shape = RoundedCornerShape(10.dp), modifier = Modifier.height(36.dp)) { Text("Invite", fontSize = 12.sp, fontFamily = Poppins, fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 5. SETTINGS & MENU
            SectionHeader("Menu")

            // MENU BARU: ECO MARKET / SHOP
            ProfileMenuItem(Icons.Rounded.ShoppingBag, "Eco Market", onClick = onNavigateToShop)
            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuItem(Icons.Rounded.Edit, "Edit Profile", onClick = onEditProfile)
            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuItem(Icons.AutoMirrored.Rounded.Logout, "Log Out", textColor = Color.Red, iconColor = Color.Red, onClick = {
                auth.signOut()
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            })

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// --- KOMPONEN KECIL ---

@Composable
fun SectionHeader(title: String) {
    Text(text = title, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
fun AchievementItem(item: Achievement) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(if (item.isUnlocked) Color(0xFFFFF9C4) else Color(0xFFE2E8F0)), contentAlignment = Alignment.Center) {
            Icon(imageVector = item.icon, contentDescription = null, tint = if (item.isUnlocked) Color(0xFFFBC02D) else Color.Gray)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = item.title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (item.isUnlocked) Color.Black else Color.Gray, fontFamily = Poppins, maxLines = 1)
    }
}

@Composable
fun ProfileStatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Text(label, fontFamily = Poppins, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit, textColor: Color = Color.Black, iconColor: Color = Color.Gray) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = textColor, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Rounded.ArrowForwardIos, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
        }
    }
}

// !! FUNGSI getTierName DIHAPUS DARI SINI !!
// Karena sudah ada di RankScreen.kt