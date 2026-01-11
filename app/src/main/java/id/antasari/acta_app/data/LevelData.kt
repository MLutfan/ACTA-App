package id.antasari.acta_app.data

enum class NodeStatus {
    LOCKED,    // Abu-abu (Belum sampai)
    CURRENT,   // Berwarna & Memantul (Sedang dikerjakan)
    COMPLETED  // Emas/Hijau (Sudah selesai)
}

data class LevelNode(
    val id: Int,
    val title: String,
    val status: NodeStatus,
    val type: String = "ACTION", // ACTION, CHEST (Hadiah), atau QUIZ
    val icon: String = "🌱"
)

// Data Dummy untuk Peta (Nanti ini diambil dari Firestore)
val dummyLevelMap = listOf(
    LevelNode(1, "Intro to Eco", NodeStatus.COMPLETED, "ACTION", "🌱"),
    LevelNode(2, "Plastic Detox", NodeStatus.COMPLETED, "ACTION", "🥤"),
    LevelNode(3, "Bonus Seeds", NodeStatus.COMPLETED, "CHEST", "💎"),
    LevelNode(4, "Save Energy", NodeStatus.CURRENT, "ACTION", "⚡"),
    LevelNode(5, "Green Transport", NodeStatus.LOCKED, "ACTION", "🚲"),
    LevelNode(6, "Quiz: Recycle", NodeStatus.LOCKED, "QUIZ", "📝"),
    LevelNode(7, "Plant a Tree", NodeStatus.LOCKED, "ACTION", "🌳"),
    LevelNode(8, "Mega Chest", NodeStatus.LOCKED, "CHEST", "🎁")
)