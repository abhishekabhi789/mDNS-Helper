package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.druk.rx2dnssd.BonjourService
import io.github.abhishekabhi789.mdnshelper.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.utils.BookmarkManager.BookMarkAction
import io.github.abhishekabhi789.mdnshelper.utils.UrlUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceInfoItem(
    modifier: Modifier = Modifier,
    info: MdnsInfo,
    onBookMarkButtonClicked: (BookMarkAction) -> Unit
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    var isBookmarked: Boolean by remember { mutableStateOf(info.isBookMarked()) }
    val bookmarkAction = if (isBookmarked) BookMarkAction.REMOVE else BookMarkAction.ADD

    Card(onClick = { expanded = !expanded }, modifier = modifier.animateContentSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .wrapContentHeight()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                FlowRow(
                    Modifier
                        .height(IntrinsicSize.Min)
                        .weight(1f)
                ) {
                    Text(
                        text = info.getServiceName(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = info.getServiceType(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = {onBookMarkButtonClicked(bookmarkAction);isBookmarked = !isBookmarked}) {
                    Icon(
                        imageVector = bookmarkAction.icon,
                        contentDescription = bookmarkAction.label
                    )
                }
            }
            if (expanded) Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier

            ) {
                UrlSection(info = info, expanded = expanded)
            } else FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                UrlSection(info = info, expanded = expanded)
            }
        }
    }
}

@Composable
fun UrlSection(modifier: Modifier = Modifier, info: MdnsInfo, expanded: Boolean) {
    val context = LocalContext.current
    info.getHostName()?.let {
        val url = UrlUtils.addressAsUrl(it.dropLast(1))
        UrlColumn(
            url = url,
            expanded = expanded,
            modifier = modifier,
            onOpenClick = { UrlUtils.browseUrl(context, url) },
            onShareClick = { UrlUtils.shareUrl(context, url) })
    }
    if (!expanded) {
        VerticalDivider()
    }
    info.getHostAddress()?.let {
        val url = UrlUtils.addressAsUrl(it)
        UrlColumn(
            url = url,
            expanded = expanded,
            modifier = modifier,
            onOpenClick = { UrlUtils.browseUrl(context, url) },
            onShareClick = { UrlUtils.shareUrl(context, url) })
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewServiceInfoItem() {
    val dummyService =
        BonjourService.Builder(0, 0, "My Local Website", "_myweb._tcp", "local.")
            .port(789)
            .hostname("test.local.")
            .build()
    val dummyInfo = MdnsInfo(dummyService)
    ServiceInfoItem(info = dummyInfo) {}
}
