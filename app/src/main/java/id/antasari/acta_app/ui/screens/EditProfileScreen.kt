package id.antasari.acta_app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.antasari.acta_app.utils.avatarList
import id.antasari.acta_app.utils.getAvatarById
import id.antasari.acta_app.ui.theme.Poppins

@Composable
fun EditProfileScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid

    // State Form
    var username by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }

    // Load Data Awal
    LaunchedEffect(Unit) {
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                username = doc.getString("name") ?: ""
                selectedAvatarId = doc.getLong("avatarId")?.toInt() ?: 1
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // HEADER
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
            }
            Text("Edit Profile", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PREVIEW AVATAR
        val currentAvatar = getAvatarById(selectedAvatarId)
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .background(currentAvatar.color.copy(alpha = 0.2f), CircleShape)
                .border(2.dp, currentAvatar.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(currentAvatar.icon, null, tint = currentAvatar.color, modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // INPUT NAMA
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PILIH AVATAR
        Text("Choose Avatar", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(avatarList) { avatar ->
                val isSelected = avatar.id == selectedAvatarId
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) avatar.color.copy(alpha = 0.3f) else Color.Gray.copy(alpha=0.1f))
                        .border(if (isSelected) 2.dp else 0.dp, if (isSelected) avatar.color else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { selectedAvatarId = avatar.id },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(avatar.icon, null, tint = avatar.color, modifier = Modifier.size(32.dp))
                    if (isSelected) {
                        Icon(Icons.Rounded.Check, null, tint = Color.Black, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // TOMBOL SAVE
        Button(
            onClick = {
                if (uid != null && username.isNotEmpty()) {
                    isLoading = true
                    db.collection("users").document(uid)
                        .update(mapOf(
                            "name" to username,
                            "avatarId" to selectedAvatarId
                        ))
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ADE80)),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White)
            else Text("SAVE CHANGES", fontFamily = Poppins, fontWeight = FontWeight.Bold)
        }
    }
}