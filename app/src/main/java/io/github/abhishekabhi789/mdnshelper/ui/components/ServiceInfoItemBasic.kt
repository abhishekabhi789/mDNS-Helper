package io.github.abhishekabhi789.mdnshelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.mdnshelper.bookmarks.BookmarkManager.BookMarkAction
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceInfoItemBasic(
    modifier: Modifier = Modifier,
    info: MdnsInfo,
    onBookMarkButtonClicked: (BookMarkAction) -> Boolean,
) {
    var isBookmarked: Boolean by remember(info) { mutableStateOf(info.isBookmarked) }
    val bookmarkAction = if (isBookmarked) BookMarkAction.REMOVE else BookMarkAction.ADD

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
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
            Icon(
                imageVector = bookmarkAction.icon,
                contentDescription = bookmarkAction.label,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        val success = onBookMarkButtonClicked(bookmarkAction)
                        if (success) isBookmarked = !isBookmarked
                    }
                    .padding(8.dp)
            )
        }
    }
}
