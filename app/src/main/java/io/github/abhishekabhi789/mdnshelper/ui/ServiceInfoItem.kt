package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.druk.rx2dnssd.BonjourService
import io.github.abhishekabhi789.mdnshelper.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.UrlUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceInfoItem(modifier: Modifier = Modifier, info: MdnsInfo) {
    val context = LocalContext.current
    val cardMargin = 8.dp
    Card(
        shape = RectangleShape,
        modifier = modifier
            .border(
                cardMargin,
                CardDefaults.cardColors().containerColor,
                CardDefaults.shape
            )
            .padding(cardMargin)
    ) {
        FlowRow(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
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
        info.getHostName()?.let {
            val url = UrlUtils.addressAsUrl(it.dropLast(1))
            UrlColumn(
                url = url,
                modifier = Modifier,
                onOpenClick = { UrlUtils.browseUrl(context, url) },
                onShareClick = { UrlUtils.shareUrl(context, url) })
        }
        info.getHostAddress()?.let {
            val url = UrlUtils.addressAsUrl(it)
            UrlColumn(
                url = url,
                modifier = Modifier,
                onOpenClick = { UrlUtils.browseUrl(context, url) },
                onShareClick = { UrlUtils.shareUrl(context, url) })
        }


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
    ServiceInfoItem(info = dummyInfo)
}
