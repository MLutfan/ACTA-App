package id.antasari.acta_app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import id.antasari.acta_app.data.AuthRepository
import id.antasari.acta_app.data.Action
import id.antasari.acta_app.data.ActionStatus
import id.antasari.acta_app.data.dummyActions
import id.antasari.acta_app.ui.theme.Poppins
import id.antasari.acta_app.utils.getAvatarById

// WARNA
val NeonGreen = Color(0xFF4ADE80)
val NeonGreenDim = Color(0xFF2E8B57)
val BackgroundColor = Color(0xFFF5F7FA)
val LockedGray = Color(0xFFE2E8F0)
val TextDark = Color(0xFF1E293B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNodeClick: (Int) -> Unit = {},
    onNavigateToMission: () -> Unit = {},
    onNavigateToShop: () -> Unit = {}
) {
    val authRepo = remember { AuthRepository() }
    val db = FirebaseFirestore.getInstance()

    var seeds by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var currentLevel by remember { mutableIntStateOf(1) }
    var avatarId by remember { mutableIntStateOf(1) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAction by remember { mutableStateOf<Action?>(null) }
    val sheetState = rememberModalBottomSheetState()

    DisposableEffect(Unit) {
        val user = authRepo.getCurrentUser()
        if (user != null) {
            val listener = db.collection("users").document(user.uid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists()) {
                        seeds = doc.getLong("seeds")?.toInt() ?: 0
                        streak = doc.getLong("currentStreak")?.toInt() ?: 0
                        currentLevel = doc.getLong("currentLevel")?.toInt() ?: 1
                        avatarId = doc.getLong("avatarId")?.toInt() ?: 1
                    }
                }
            onDispose { listener.remove() }
        } else onDispose { }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            ActaModernTopBar(
                streak = streak,
                seeds = seeds,
                avatarId = avatarId,
                onSeedsClick = onNavigateToShop // <--- Konek ke Shop
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            BackgroundDecoration()
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)) {
                    DailyMissionBanner(onClick = onNavigateToMission)
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dynamicActions = dummyActions.map { action ->
                        val dynamicStatus = when {
                            action.id < currentLevel -> ActionStatus.COMPLETED
                            action.id == currentLevel -> ActionStatus.ACTIVE
                            else -> ActionStatus.LOCKED
                        }
                        action.copy(status = dynamicStatus)
                    }.reversed()

                    itemsIndexed(dynamicActions) { index, action ->
                        val isLastItem = index == dynamicActions.lastIndex
                        TimelineNode(
                            action = action,
                            isLastItem = isLastItem,
                            onClick = {
                                selectedAction = action
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
            if (showBottomSheet && selectedAction != null) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White
                ) {
                    MissionDetailContent(
                        action = selectedAction!!,
                        onStartClick = {
                            showBottomSheet = false
                            onNodeClick(selectedAction!!.id)
                        }
                    )
                }
            }
        }
    }
}

// --- PERBAIKAN TOP BAR & STAT CHIP ---

@Composable
fun ActaModernTopBar(
    streak: Int,
    seeds: Int,
    avatarId: Int,
    onSeedsClick: () -> Unit // Parameter klik
) {
    val currentAvatar = getAvatarById(avatarId)

    Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(horizontal = 24.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {

            // Streak (Tidak bisa diklik)
            StatChip(icon = Icons.Rounded.LocalFireDepartment, value = "$streak", color = Color(0xFFFF5722))

            Spacer(modifier = Modifier.width(8.dp))

            // Seeds (BISA DIKLIK)
            // Kita pass onClick langsung ke StatChip
            StatChip(
                icon = Icons.Rounded.EnergySavingsLeaf,
                value = "$seeds",
                color = Color(0xFFFFD700),
                onClick = onSeedsClick // <--- PENTING
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = currentAvatar.color.copy(alpha = 0.2f),
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = currentAvatar.icon, contentDescription = null, tint = currentAvatar.color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Hello, Eco-Warrior!", fontSize = 14.sp, color = Color.Gray, fontFamily = Poppins)
                Text(text = "Every action matters.", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextDark, fontFamily = Poppins)
            }
        }
    }
}

// UPDATE: StatChip sekarang menerima onClick (default null/empty)
@Composable
fun StatChip(
    icon: ImageVector,
    value: String,
    color: Color,
    onClick: (() -> Unit)? = null // Parameter Optional
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        // Modifier clickable ditaruh di sini agar Surface menangkap sentuhan
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Poppins, color = TextDark)
        }
    }
}

// ... Sisanya (Banner, Decoration, MissionContent, Timeline) tidak berubah ...
@Composable
fun DailyMissionBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TextDark)
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.TaskAlt, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Misi Harian", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Poppins)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Lihat target & rewards hari ini.", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp, fontFamily = Poppins)
            }
            Box(modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun BackgroundDecoration() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(color = NeonGreen.copy(alpha = 0.05f), radius = 150.dp.toPx(), center = Offset(0f, 100.dp.toPx()))
        drawCircle(color = Color(0xFF29B6F6).copy(alpha = 0.05f), radius = 100.dp.toPx(), center = Offset(size.width, size.height * 0.4f))
        drawCircle(color = Color.Gray.copy(alpha = 0.03f), radius = 60.dp.toPx(), center = Offset(40.dp.toPx(), size.height * 0.8f))
    }
}

@Composable
fun MissionDetailContent(action: Action, onStartClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp).padding(bottom = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(80.dp).background(BackgroundColor, CircleShape).padding(16.dp), contentAlignment = Alignment.Center) {
            Icon(imageVector = getIconForAction(action.id), contentDescription = null, tint = NeonGreenDim, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = action.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = Poppins)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = action.description, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp), fontFamily = Poppins)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp)).border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, null, tint = Color(0xFF29B6F6))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("EXP", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                    Text("+${action.expReward}", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                }
            }
            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.LightGray))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.EnergySavingsLeaf, null, tint = Color(0xFFFFD700))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("SEEDS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                    Text("+${action.seedReward}", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins)
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = onStartClick, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.White), shape = RoundedCornerShape(12.dp), enabled = action.status != ActionStatus.LOCKED) {
            Text(text = if (action.status == ActionStatus.LOCKED) "Locked" else "AMBIL FOTO BUKTI", fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = Poppins)
        }
    }
}

@Composable
fun TimelineNode(action: Action, isLastItem: Boolean, onClick: () -> Unit) {
    val isCurrent = action.status == ActionStatus.ACTIVE
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(if (isCurrent) 180.dp else 120.dp)) {
        if (!isLastItem) {
            Canvas(modifier = Modifier.width(2.dp).fillMaxHeight().align(Alignment.Center).offset(y = if (isCurrent) (60).dp else (50).dp)) {
                drawLine(color = if(action.status == ActionStatus.COMPLETED) NeonGreenDim.copy(alpha=0.5f) else LockedGray, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f), cap = StrokeCap.Round)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = "Action ${action.id}", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp), fontFamily = Poppins)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (isCurrent) 100.dp else 64.dp).shadow(elevation = if (isCurrent) 15.dp else 0.dp, spotColor = NeonGreen.copy(alpha = 0.6f), shape = CircleShape).clip(CircleShape).background(Color.White).border(width = if (isCurrent) 4.dp else 0.dp, color = if (isCurrent) NeonGreen else Color.Transparent, shape = CircleShape).clickable(enabled = action.status != ActionStatus.LOCKED) { onClick() }) {
                Box(modifier = Modifier.fillMaxSize().padding(6.dp).clip(CircleShape).background(when (action.status) { ActionStatus.COMPLETED -> NeonGreenDim; ActionStatus.ACTIVE -> NeonGreen; ActionStatus.LOCKED -> LockedGray }), contentAlignment = Alignment.Center) {
                    if (isCurrent) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Rounded.PlayArrow, "Start", tint = Color.White, modifier = Modifier.size(32.dp)); Text("START", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = Poppins) }
                    } else if (action.status == ActionStatus.COMPLETED) { Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    } else { Icon(Icons.Rounded.Lock, null, tint = Color.Gray, modifier = Modifier.size(24.dp)) }
                }
            }
            if (action.status != ActionStatus.LOCKED) { Spacer(modifier = Modifier.height(8.dp)); Text(text = action.title, fontWeight = FontWeight.Bold, fontSize = if(isCurrent) 16.sp else 12.sp, color = if(isCurrent) Color.Black else Color.Gray, fontFamily = Poppins) }
        }
    }
}

fun getIconForAction(id: Int): ImageVector {
    return when (id % 5) { 1 -> Icons.Rounded.WaterDrop; 2 -> Icons.Rounded.Bolt; 3 -> Icons.AutoMirrored.Rounded.DirectionsWalk; 4 -> Icons.Rounded.Recycling; 0 -> Icons.Rounded.Forest; else -> Icons.Rounded.Star }
}