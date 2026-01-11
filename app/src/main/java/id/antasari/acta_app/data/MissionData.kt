package id.antasari.acta_app.data

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val type: String, // "Daily" atau "Weekly"
    val iconEmoji: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val isCompleted: Boolean = false
)

// Data Dummy biar tidak error merah
val dummyMissions = listOf(
    Mission("1", "Bring Tumbler", "Use your own bottle.", 50, "Daily", "🥤", "Easy"),
    Mission("2", "No Plastic Straw", "Refuse plastic straws.", 30, "Daily", "🚫", "Easy"),
    Mission("3", "Walk to Campus", "Reduce carbon footprint.", 100, "Daily", "🚶", "Medium"),
    Mission("4", "Recycle Paper", "Recycle used paper.", 150, "Weekly", "♻️", "Medium"),
    Mission("5", "Plant a Tree", "Plant one tree.", 500, "Weekly", "🌳", "Hard")
)