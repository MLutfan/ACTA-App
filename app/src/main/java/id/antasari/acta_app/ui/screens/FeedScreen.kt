package id.antasari.acta_app.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import id.antasari.acta_app.ui.theme.Poppins
import id.antasari.acta_app.utils.getAvatarById

// Model Data Post
data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val userAvatarId: Int,
    val imageUrl: String,
    val actionTitle: String,
    val likes: Int,
    val timestamp: Timestamp?,
    val likedBy: List<String> = emptyList()
)

@Composable
fun FeedScreen() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserUid = auth.currentUser?.uid

    // State
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Realtime Listener
    DisposableEffect(Unit) {
        val query = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Urutkan dari yang terbaru
            .limit(20)

        val listener = query.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            if (snapshot != null) {
                val list = snapshot.documents.map { doc ->
                    Post(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "Unknown",
                        userAvatarId = doc.getLong("userAvatarId")?.toInt() ?: 1,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        actionTitle = doc.getString("actionTitle") ?: "Completed a mission",
                        likes = doc.getLong("likes")?.toInt() ?: 0,
                        timestamp = doc.getTimestamp("timestamp"),
                        likedBy = (doc.get("likedBy") as? List<String>) ?: emptyList()
                    )
                }
                posts = list
                isLoading = false
            }
        }
        onDispose { listener.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // HEADER SEDERHANA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "Community Feed",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B)
            )
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4ADE80))
            }
        } else if (posts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Belum ada postingan.\nJadilah yang pertama!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        currentUserId = currentUserUid,
                        onLikeClick = {
                            if (currentUserUid != null) {
                                toggleLike(db, post.id, currentUserUid, post.likedBy.contains(currentUserUid))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    currentUserId: String?,
    onLikeClick: () -> Unit
) {
    val avatar = getAvatarById(post.userAvatarId)
    val isLiked = post.likedBy.contains(currentUserId)

    // Hitung Waktu
    val timeAgo = if (post.timestamp != null) {
        DateUtils.getRelativeTimeSpanString(
            post.timestamp.toDate().time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    } else "Just now"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // 1. HEADER POST
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatar.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(avatar.icon, null, tint = avatar.color, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.username,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = timeAgo,
                        fontFamily = Poppins,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            // 2. GAMBAR POST
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Post Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray)
            )

            // 3. FOOTER
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${post.likes} Likes",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Menyelesaikan misi: ",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = Color.Gray
                    // parameter 'display' sudah dihapus, error akan hilang
                )
                Text(
                    text = post.actionTitle,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

fun toggleLike(db: FirebaseFirestore, postId: String, userId: String, isLiked: Boolean) {
    val postRef = db.collection("posts").document(postId)

    if (isLiked) {
        postRef.update(
            "likes", FieldValue.increment(-1),
            "likedBy", FieldValue.arrayRemove(userId)
        )
    } else {
        postRef.update(
            "likes", FieldValue.increment(1),
            "likedBy", FieldValue.arrayUnion(userId)
        )
    }
}