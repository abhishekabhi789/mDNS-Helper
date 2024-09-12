package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.abhishekabhi789.mdnshelper.MdnsInfo

@Composable
fun ServiceInfoItem(modifier: Modifier = Modifier, info: MdnsInfo, onResolveCallback: () -> Unit) {
    Row(
        Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()) {
        Column(modifier = modifier) {
            Text(info.getServiceType(), style = MaterialTheme.typography.titleMedium)
            Text(info.getServiceName(), style = MaterialTheme.typography.bodySmall)
            info.getHostAddress()?.let { Text(it) }
            info.getPort()?.let { Text(it.toString()) }

        }
        Spacer(modifier = Modifier.weight(1f))
        Column {
            Text(
                text = info.getResolverStatus().toString().lowercase()
                    .replaceFirstChar { it.uppercase() })
            when (info.getResolverStatus()) {
                MdnsInfo.ResolverStatus.NOT_RESOLVED, MdnsInfo.ResolverStatus.FAILED -> {
                    Button(onClick = onResolveCallback) {
                        Text(text = "Resolve")
                    }
                }
                MdnsInfo.ResolverStatus.RESOLVED -> {
                    Text(text = info.getHostAddress()!!)
                    Text(text = info.getPort().toString())
                }
                else->{}
            }
        }
    }
}
