package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UrlColumn(
    modifier: Modifier = Modifier,
    url: String,
    onOpenClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChipButton(label = "Browse", icon =Icons.Default.OpenInBrowser, onClick = onOpenClick )
            ChipButton(label = "Share", icon = Icons.Default.Share, onClick = onShareClick)
        }
    }
}
