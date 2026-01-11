package id.antasari.acta_app.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Data Class untuk Avatar
data class AvatarOption(
    val id: Int,
    val icon: ImageVector,
    val color: Color,
    val name: String
)

// List Pilihan Avatar (Bisa ditambah nanti)
val avatarList = listOf(
    AvatarOption(1, Icons.Rounded.Face, Color(0xFF4ADE80), "Human"),
    AvatarOption(2, Icons.Rounded.Pets, Color(0xFFFFAB91), "Cat"),
    AvatarOption(3, Icons.Rounded.CrueltyFree, Color(0xFFFFF59D), "Rabbit"),
    AvatarOption(4, Icons.Rounded.EmojiNature, Color(0xFFA5D6A7), "Bee"),
    AvatarOption(5, Icons.Rounded.Forest, Color(0xFF81C784), "Tree Spirit"),
    AvatarOption(6, Icons.Rounded.WaterDrop, Color(0xFF4FC3F7), "Water Drop")
)

fun getAvatarById(id: Int): AvatarOption {
    return avatarList.find { it.id == id } ?: avatarList[0] // Default Human
}