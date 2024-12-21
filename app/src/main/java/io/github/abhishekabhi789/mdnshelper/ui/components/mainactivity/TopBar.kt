package io.github.abhishekabhi789.mdnshelper.ui.components.mainactivity

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.ui.activities.AboutActivity
import io.github.abhishekabhi789.mdnshelper.ui.activities.SettingsActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier, scrollBehavior: TopAppBarScrollBehavior) {
    val context = LocalContext.current
    var showMenu: Boolean by remember { mutableStateOf(false) }
    LargeTopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = stringResource(R.string.topbar_more_option_button_description)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.title_settings_activity)) },
                        leadingIcon = { Icon(Icons.Default.Settings, null) },
                        onClick = {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        })
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.title_activity_about)) },
                        leadingIcon = { Icon(Icons.Default.Info, null) },
                        onClick = {
                            context.startActivity(Intent(context, AboutActivity::class.java))
                        })
                }
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}
