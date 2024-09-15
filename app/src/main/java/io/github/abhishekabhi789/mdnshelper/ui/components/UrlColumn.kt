package io.github.abhishekabhi789.mdnshelper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UrlColumn(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    url: String,
    onOpenClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(modifier = modifier.animateContentSize()) {
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        AnimatedVisibility(visible = expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChipButton(
                    label = "Browse",
                    onClick = onOpenClick
                )
                ChipButton(label = "Share", onClick = onShareClick)
            }
        }
    }
}
