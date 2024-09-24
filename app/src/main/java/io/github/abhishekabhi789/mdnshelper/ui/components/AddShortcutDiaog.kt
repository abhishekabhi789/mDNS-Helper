package io.github.abhishekabhi789.mdnshelper.ui.components

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.viewmodel.MainActivityViewmodel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddShortcutDialog(
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewmodel,
    info: MdnsInfo,
    pickNewIcon: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current as Activity
    val cardDefaults = CardDefaults
    val shortcutIconList by viewModel.shortcutIcons.collectAsState()
    var isIconDeletionMode by remember { mutableStateOf(false) }
    val deleteButton by remember {
        derivedStateOf {
            Icons.Default.let { icons ->
                if (isIconDeletionMode && shortcutIconList.isNotEmpty()) Pair(icons.Done, "Done")
                else Pair(icons.Delete, "Delete")
            }
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.background(cardDefaults.cardColors().containerColor, cardDefaults.shape)
    ) {
        val defaultIcon: Bitmap = remember {
            (context.getDrawable(R.drawable.ic_launcher_foreground)
                ?: ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground))
                ?.toBitmap(config = Bitmap.Config.ARGB_8888)
                ?: BitmapFactory.decodeResource(
                    context.resources, R.drawable.ic_launcher_foreground
                )
        }

        var selectedIconBitmap: Bitmap by remember { mutableStateOf(defaultIcon) }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Add shortcut", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Would you like to create a home screen shortcut for ${info.getServiceName()}?",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Choose an icon")
                Spacer(modifier = Modifier.weight(1f))
                if (shortcutIconList.isNotEmpty()) {
                    IconButton(onClick = { isIconDeletionMode = !isIconDeletionMode }) {
                        Icon(
                            imageVector = deleteButton.first,
                            contentDescription = deleteButton.second,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                stickyHeader {
                    val addIcon = remember {
                        context.getDrawable(R.drawable.ic_add_new)
                            ?.toBitmap(config = Bitmap.Config.ARGB_8888)
                    }
                    addIcon?.let {
                        ShortcutIcon(bitmap = it, isSelected = false, onClick = { pickNewIcon() })
                    }
                }
                defaultIcon.let { bitmap ->
                    item {
                        ShortcutIcon(bitmap = bitmap,
                            isSelected = bitmap == selectedIconBitmap,
                            onClick = { selectedIconBitmap = bitmap }
                        )
                    }
                }
                items(shortcutIconList) { iconBitmap ->
                    ShortcutIcon(
                        modifier = Modifier,
                        bitmap = iconBitmap,
                        isSelected = selectedIconBitmap == iconBitmap,
                        isDeletionMode = isIconDeletionMode,
                        onClick = {
                            if (isIconDeletionMode) {
                                viewModel.deleteIcon(context, iconBitmap)
                            } else {
                                selectedIconBitmap = iconBitmap
                            }
                        }
                    )
                }
            }

            Row {
                OutlinedButton(onClick = onDismiss) {
                    Text(text = "Cancel")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    viewModel.addPinnedShortcut(info, selectedIconBitmap)
                    onDismiss()
                }) {
                    Text(text = "Create shortcut")
                }
            }
        }
    }
}

@Composable
fun ShortcutIcon(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    isSelected: Boolean,
    isDeletionMode: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val selectedModifier: Modifier =
        if (isSelected) Modifier
            .border(2.dp, MaterialTheme.colorScheme.onPrimaryContainer, shape)
            .border(6.dp, Color.Transparent, shape)
        else Modifier
    Box(modifier = modifier) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .aspectRatio(1f)
                .then(selectedModifier)
                .padding(6.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.primary, shape)
                .clickable { onClick() }
        )
        if (isDeletionMode) Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Delete icon",
            modifier = Modifier
                .background(Color.Red, CircleShape)
                .align(Alignment.TopEnd)
        )
    }
}
