package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ChipButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    AssistChip(onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null)
        }
    )
}
