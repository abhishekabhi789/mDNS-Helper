package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ChipButton(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick,
        label = { Text(text = label) },
    )
}
