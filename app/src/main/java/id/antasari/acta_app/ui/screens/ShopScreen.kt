package id.antasari.acta_app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// Data Model Barang Dagangan
data class ShopItem(
    val id: String,
    val name: String,
    val price: Int,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun ShopScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val authRepo = AuthRepository()

    // STATE
    var mySeeds by remember { mutableIntStateOf(0) }
    var myInventory by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Ambil Data User (Seeds & Inventory)
    DisposableEffect(Unit) {
        val user = authRepo.getCurrentUser()
        if (user != null) {
            val listener = db.collection("users").document(user.uid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        mySeeds = doc.getLong("seeds")?.toInt() ?: 0
                        // Ambil array inventory, kalau null anggap kosong
                        myInventory = (doc.get("inventory") as? List<String>) ?: emptyList()
                    }
                }
            onDispose { listener.remove() }
        } else onDispose { }
    }

    // DAFTAR BARANG DAGANGAN
    val shopItems = listOf(
        ShopItem("frame_gold", "Golden Frame", 500, Icons.Rounded.CheckCircle, Color(0xFFFFD700)),
        ShopItem("frame_neon", "Neon Aura", 1000, Icons.Rounded.EnergySavingsLeaf, Color(0xFF4ADE80)),
        ShopItem("streak_freeze", "Streak Freeze", 200, Icons.Rounded.Lock, Color(0xFF29B6F6)),
        ShopItem("donation_tree", "Tanam 1 Pohon", 2500, Icons.Rounded.EnergySavingsLeaf, Color(0xFF43A047))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // 1. HEADER (Saldo Seeds)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4ADE80), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(24.dp)
        ) {
            Column {
                // Tombol Back
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.White.copy(alpha=0.2f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ShoppingBag, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Eco Market", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                        Text("Tukar seeds dengan item keren!", fontFamily = Poppins, fontSize = 14.sp, color = Color.White.copy(alpha=0.8f))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Display Saldo Besar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Rounded.EnergySavingsLeaf, null, tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$mySeeds Seeds",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. GRID ITEMS
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(shopItems) { item ->
                // Cek apakah user sudah punya item ini
                val isConsumable = item.id == "streak_freeze" || item.id.startsWith("donation")
                val isOwned = !isConsumable && myInventory.contains(item.id)

                ShopItemCard(
                    item = item,
                    isOwned = isOwned,
                    canAfford = mySeeds >= item.price,
                    onBuy = {
                        if (isLoading) return@ShopItemCard

                        val user = authRepo.getCurrentUser()
                        if (user != null) {
                            isLoading = true

                            val userRef = db.collection("users").document(user.uid)

                            db.runTransaction { transaction ->
                                // Baca saldo terbaru
                                val snapshot = transaction.get(userRef)
                                val currentSeeds = snapshot.getLong("seeds")?.toInt() ?: 0

                                if (currentSeeds >= item.price) {
                                    // Potong Saldo
                                    transaction.update(userRef, "seeds", FieldValue.increment(-item.price.toLong()))

                                    // Tambah ke inventory (jika bukan consumable)
                                    if (!isConsumable) {
                                        transaction.update(userRef, "inventory", FieldValue.arrayUnion(item.id))
                                    }
                                } else {
                                    // PERBAIKAN DI SINI: Gunakan Exception biasa
                                    throw Exception("Saldo tidak cukup")
                                }
                            }.addOnSuccessListener {
                                isLoading = false
                                Toast.makeText(context, "Berhasil membeli ${item.name}!", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                isLoading = false
                                // Pesan error akan muncul di sini (misal: "Saldo tidak cukup")
                                Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Item
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(item.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(item.name, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Beli / Status
            Button(
                onClick = onBuy,
                enabled = !isOwned && canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOwned) Color.Gray else Color(0xFF4ADE80),
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isOwned) {
                    Text("OWNED", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.EnergySavingsLeaf, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${item.price}", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                    }
                }
            }
        }
    }
}