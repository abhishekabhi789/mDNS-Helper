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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.mdnshelper.R

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
                    label = stringResource(R.string.service_info_url_section_browse_button_label),
                    onClick = onOpenClick
                )
                ChipButton(
                    label = stringResource(R.string.service_info_url_section_share_button_label),
                    onClick = onShareClick
                )
            }
        }
    }
}
