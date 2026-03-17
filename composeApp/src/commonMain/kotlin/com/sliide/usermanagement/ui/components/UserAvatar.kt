package com.sliide.usermanagement.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val avatarPalette = listOf(
    Color(0xFF1565C0), // blue
    Color(0xFF2E7D32), // green
    Color(0xFF6A1B9A), // purple
    Color(0xFFAD1457), // pink
    Color(0xFF00695C), // teal
    Color(0xFFE65100), // deep orange
    Color(0xFF4527A0), // deep purple
    Color(0xFF558B2F), // light green
)

private fun avatarColor(name: String): Color {
    val index = name.fold(0) { acc, c -> acc * 31 + c.code }
        .and(Int.MAX_VALUE) % avatarPalette.size
    return avatarPalette[index]
}

@Composable
fun UserAvatar(
    name: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")

    Surface(
        modifier = modifier
            .size(size)
            .shadow(elevation = 3.dp, shape = CircleShape, clip = false),
        shape = CircleShape,
        color = avatarColor(name)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = if (size > 60.dp) MaterialTheme.typography.headlineMedium
                        else MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserAvatarPreview() {
    MaterialTheme {
        UserAvatar(name = "Jane Smith")
    }
}

@Preview(showBackground = true)
@Composable
private fun UserAvatarLargePreview() {
    MaterialTheme {
        UserAvatar(name = "Jane Smith", size = 88.dp)
    }
}
