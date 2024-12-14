package io.github.abhishekabhi789.mdnshelper.ui.components.mainactivity

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExtraInfoList(modifier: Modifier = Modifier, extraMap: Map<String, String>) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val extraInfo = remember(extraMap) { extraMap.toList() }
    Text(
        text = "Extra info",
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .padding(horizontal = 16.dp)
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
            Text(text = "No extra info found for this service")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.padding(16.dp)
        ) {
            stickyHeader {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(text = "Copy all as JSON")
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            val json = JSONObject(extraMap).toString()
                            clipboardManager.setText(AnnotatedString(json))
                            Toast
                                .makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                                .show()
                        },
                        colors = IconButtonDefaults.outlinedIconButtonColors()
                            .copy(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy as JSON"
                        )
                    }
                }
            }
            items(extraInfo) { (key, value) ->
                ExtraInfo(modifier = Modifier, key, value)
            }
        }
    }
}

@Composable
fun ExtraInfo(modifier: Modifier = Modifier, key: String, value: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
            .clickable {
                clipboardManager.setText(AnnotatedString("$key: $value"))
                Toast
                    .makeText(context, "$key copied to clipboard", Toast.LENGTH_SHORT)
                    .show()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
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

@Preview(showBackground = true)
@Composable
fun PreviewExtraInfoList() {
    val dummyValues = mapOf(Pair("Key 1", "Value 1"), Pair("Key 2", "Value 2"))
    ExtraInfoList(extraMap = dummyValues)
}
