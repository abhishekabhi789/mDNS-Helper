package io.github.abhishekabhi789.mdnshelper.ui.components.mainactivity

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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.utils.UrlUtils
import io.github.abhishekabhi789.mdnshelper.viewmodel.MainActivityViewmodel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AddShortcutScreen(
    modifier: Modifier = Modifier,
    activityContext: Activity,
    viewModel: MainActivityViewmodel,
    info: MdnsInfo,
    pickNewIcon: () -> Unit,
    onDismiss: () -> Unit
) {
    val shortcutIconList by viewModel.shortcutIcons.collectAsState()
    var preferredBrowser: BrowserChoice by remember { mutableStateOf(BrowserChoice.Default) }
    val browsersAvailable: List<BrowserChoice.InstalledBrowser> =
        remember { UrlUtils.getBrowsers(activityContext) }
    var isIconDeletionMode by remember { mutableStateOf(false) }
    val deleteButton by remember {
        derivedStateOf {
            Icons.Default.let { icons ->
                if (isIconDeletionMode && shortcutIconList.isNotEmpty()) Pair(
                    icons.Done,
                    R.string.shortcut_icon_exit_deletion_state
                )
                else Pair(icons.Delete, R.string.shortcut_icon_enter_deletion_state)
            }
        }
    }
    val defaultIcon: Bitmap = remember {
        (activityContext.getDrawable(R.drawable.ic_launcher_foreground)
            ?: ContextCompat.getDrawable(activityContext, R.drawable.ic_launcher_foreground))
            ?.toBitmap(config = Bitmap.Config.ARGB_8888)
            ?: BitmapFactory.decodeResource(
                activityContext.resources, R.drawable.ic_launcher_foreground
            )
    }

    var selectedIconBitmap: Bitmap by remember { mutableStateOf(defaultIcon) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            stringResource(R.string.add_shortcut_dialog_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.add_shortcut_dialog_description, info.getServiceName()),
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.add_shortcut_dialog_choose_icon),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            if (shortcutIconList.isNotEmpty()) {
                IconButton(onClick = { isIconDeletionMode = !isIconDeletionMode }) {
                    Icon(
                        imageVector = deleteButton.first,
                        contentDescription = stringResource(deleteButton.second),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            stickyHeader {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .border(8.dp, MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(8.dp)
                ) {
                    OutlinedIconButton(
                        onClick = {
                            isIconDeletionMode = false
                            pickNewIcon()
                        },
                        modifier = Modifier
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_shortcut_dialog_pick_new_icon),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
                            viewModel.deleteIcon(activityContext, iconBitmap)
                        } else {
                            selectedIconBitmap = iconBitmap
                        }
                    }
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            var dropDownExpanded by remember { mutableStateOf(false) }

            Text(
                text = stringResource(R.string.add_shortcut_dialog_preferred_browser),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            ExposedDropdownMenuBox(
                expanded = dropDownExpanded,
                onExpandedChange = { dropDownExpanded = it }) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                ) {
                    Text(
                        text = stringResource(preferredBrowser.choiceLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropDownExpanded)
                }
                ExposedDropdownMenu(
                    expanded = dropDownExpanded,
                    onDismissRequest = { dropDownExpanded = false },
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    listOf(
                        BrowserChoice.Default,
                        BrowserChoice.AskUser,
                        BrowserChoice.CustomTab
                    ).forEach { browser ->
                        DropdownMenuItem(
                            text = { Text(text = stringResource(browser.choiceLabel)) },
                            onClick = {
                                preferredBrowser = browser
                                dropDownExpanded = false
                            })
                    }
                    browsersAvailable.forEach { browser ->
                        DropdownMenuItem(
                            text = { Text(text = browser.appName) },
                            leadingIcon = {
                                Image(
                                    bitmap = browser.appIcon.asImageBitmap(),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                                        setToSaturation(0f)
                                    }),
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .size(InputChipDefaults.IconSize)
                                )
                            },
                            onClick = {
                                preferredBrowser = browser
                                dropDownExpanded = false
                            },
                        )
                    }
                }
            }
        }

        Row {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_shortcut_dialog_cancel_operation))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                viewModel.addPinnedShortcut(info, selectedIconBitmap, preferredBrowser)
                onDismiss()
            }) {
                Text(text = stringResource(R.string.add_shortcut_dialog_create_shortcut))
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
            contentDescription = stringResource(R.string.shortcut_icon_deletion_state_description),
            modifier = Modifier
                .background(Color.Red, CircleShape)
                .align(Alignment.TopEnd)
        )
    }
}
