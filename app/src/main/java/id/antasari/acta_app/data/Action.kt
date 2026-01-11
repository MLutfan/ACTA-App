package id.antasari.acta_app.data

enum class ActionStatus {
    LOCKED, ACTIVE, COMPLETED
}

data class Action(
    val id: Int,
    val title: String,       // Contoh: "Hemat Air"
    val description: String, // Penjelasan detail misi
    val expReward: Int,      // Hadiah XP
    val seedReward: Int,     // Hadiah Seeds (Mata Uang) <-- KITA TAMBAH INI
    val status: ActionStatus
)

// Data Dummy (Sudah saya update dengan seedReward)
val dummyActions = listOf(
    Action(1, "Bawa Tumbler", "Kurangi sampah plastik dengan membawa botol minum sendiri hari ini.", 15, 10, ActionStatus.COMPLETED),
    Action(2, "Matikan Lampu", "Matikan lampu kamar saat tidak digunakan di siang hari.", 20, 15, ActionStatus.ACTIVE), // Level saat ini
    Action(3, "Jalan Kaki", "Pergi ke kampus/kantor dengan jalan kaki atau sepeda.", 30, 25, ActionStatus.LOCKED),
    Action(4, "Pilah Sampah", "Pisahkan sampah organik dan anorganik di rumahmu.", 35, 30, ActionStatus.LOCKED),
    Action(5, "Tanam Pohon", "Tanam satu bibit pohon di halaman rumah atau pot.", 100, 50, ActionStatus.LOCKED),
    Action(6, "Hapus Email", "Hapus 50 email spam untuk mengurangi jejak karbon server.", 15, 5, ActionStatus.LOCKED),
    Action(7, "Makan Sayur", "Ganti menu daging dengan sayuran untuk satu kali makan.", 25, 20, ActionStatus.LOCKED),
    Action(8, "Matikan Keran", "Jangan biarkan air mengalir saat menggosok gigi.", 10, 5, ActionStatus.LOCKED),
    Action(9, "Tas Belanja", "Bawa tas kain sendiri saat belanja ke minimarket.", 20, 10, ActionStatus.LOCKED),
    Action(10, "Share Edukasi", "Bagikan tips lingkungan di media sosialmu.", 50, 40, ActionStatus.LOCKED)
)