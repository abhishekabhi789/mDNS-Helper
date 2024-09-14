package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.mdnshelper.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.utils.BookmarkManager.BookMarkAction

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceInfoItemBasic(
    modifier: Modifier = Modifier,
    info: MdnsInfo,
    onBookMarkButtonClicked: (BookMarkAction) -> Boolean,
) {
    var isBookmarked: Boolean by remember(info) { mutableStateOf(info.isBookmarked) }
    val bookmarkAction = if (isBookmarked) BookMarkAction.REMOVE else BookMarkAction.ADD

    Column(verticalArrangement = Arrangement.Top, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .wrapContentHeight()
                    .weight(1f)
            ) {
                Text(
                    text = info.getServiceName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(modifier = Modifier.weight(1f)) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = info.getServiceType(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            IconButton(onClick = {
                val success = onBookMarkButtonClicked(bookmarkAction)
                if (success) isBookmarked = !isBookmarked
            }) {
                Icon(
                    imageVector = bookmarkAction.icon,
                    contentDescription = bookmarkAction.label
                )
            }
        }
    }
}
