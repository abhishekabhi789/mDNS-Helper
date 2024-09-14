package io.github.abhishekabhi789.mdnshelper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.abhishekabhi789.mdnshelper.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.utils.BookmarkManager.BookMarkAction

@Composable
fun UnReachableBookmarks(
    modifier: Modifier = Modifier,
    info: MdnsInfo,
    onBookMarkButtonClicked: (BookMarkAction) -> Boolean
) {
    Card(modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp)
                .wrapContentHeight()
        ) {
            ServiceInfoItemBasic(info = info, onBookMarkButtonClicked = onBookMarkButtonClicked)
        }
    }
}

@Preview
@Composable
fun PreviewUnReachableBookmarks() {
    UnReachableBookmarks(info = getDummyServiceInfo()){true}
}
