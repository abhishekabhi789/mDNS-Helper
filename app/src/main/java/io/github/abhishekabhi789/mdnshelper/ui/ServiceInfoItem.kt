package io.github.abhishekabhi789.mdnshelper.ui

import android.os.Build
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import io.github.abhishekabhi789.mdnshelper.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.utils.BookmarkManager.BookMarkAction
import io.github.abhishekabhi789.mdnshelper.utils.UrlUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceInfoItem(
    modifier: Modifier = Modifier,
    info: MdnsInfo,
    onBookMarkButtonClicked: (BookMarkAction) -> Boolean,
    onShortcutButtonClicked: () -> Unit
) {
    var expanded: Boolean by remember { mutableStateOf(false) }
    val cardColors = CardDefaults.cardColors().apply {
        if (expanded) this.copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
    }

    Card(
        colors = cardColors,
        onClick = { expanded = !expanded },
        modifier = modifier.animateContentSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
                .wrapContentHeight()
        ) {
            ServiceInfoItemBasic(info = info, onBookMarkButtonClicked = onBookMarkButtonClicked)
            if (expanded) Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
            ) {
                UrlSection(info = info, expanded = expanded)
            } else FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                UrlSection(info = info, expanded = expanded)
            }
            if (expanded) {
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ChipButton(label = "Add shortcut", onClick = onShortcutButtonClicked)
                    }
                }
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
    ServiceInfoItem(
        info = getDummyServiceInfo(),
        onBookMarkButtonClicked = { true },
        onShortcutButtonClicked = { })
}
