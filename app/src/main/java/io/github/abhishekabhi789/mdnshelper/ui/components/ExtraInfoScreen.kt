package io.github.abhishekabhi789.mdnshelper.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo

@Composable
fun ExtraInfoScreen(modifier: Modifier = Modifier, info: MdnsInfo) {
    val extraInfo = remember { info.bonjourService.txtRecords.toList() }

    Text(
        text = "Extra info",
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .padding(16.dp)
            .padding(start = 16.dp)
    )
    if (extraInfo.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Text(text = "No additional info found for this service")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.padding(16.dp)
        ) {
            items(extraInfo) { (key, value) ->
                ExtraInfo(key, value)
            }
        }
    }
}

@Composable
fun ExtraInfo(key: String, value: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
            .clickable {
                clipboardManager.setText(AnnotatedString("$key: $value"))
                Toast
                    .makeText(context, "$key copied to clipboard", Toast.LENGTH_SHORT)
                    .show()
            }
            .padding(8.dp)

    ) {
        Text(
            text = key,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
