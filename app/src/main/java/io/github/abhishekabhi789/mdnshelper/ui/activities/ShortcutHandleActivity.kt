package io.github.abhishekabhi789.mdnshelper.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice
import io.github.abhishekabhi789.mdnshelper.shortcut.ShortcutManager
import io.github.abhishekabhi789.mdnshelper.ui.theme.MDNSHelperTheme
import io.github.abhishekabhi789.mdnshelper.utils.CustomTabsHelper
import io.github.abhishekabhi789.mdnshelper.utils.UrlUtils
import io.github.abhishekabhi789.mdnshelper.viewmodel.ShortcutHandleViewmodel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShortcutHandleActivity @Inject constructor() : ComponentActivity() {
    private val viewModel: ShortcutHandleViewmodel by viewModels()

    @Inject
    lateinit var customTabsHelper: CustomTabsHelper

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.processShortcutAction(intent)
        lifecycleScope.launch {
            viewModel.currentAddress.collectLatest { address ->
                openUrl(address)
            }
        }
        lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { errorMsg ->
                Log.e(TAG, "onCreate: error $errorMsg")
                val toast = Toast(this@ShortcutHandleActivity)
                toast.setText(errorMsg)
                toast.show()
                delay(toast.duration.toLong())
                finish()
            }
        }
        setContent {
            val scannerProgress: Float by viewModel.resolvingProgress.collectAsState()
            val address by viewModel.currentAddress.collectAsState()
            MDNSHelperTheme {
                Surface {
                    CardDefaults.let { card ->
                        BasicAlertDialog(
                            onDismissRequest = { finish() },
                            modifier = Modifier
                                .background(card.cardColors().containerColor, card.shape)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .animateContentSize()
                            ) {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (address.isNullOrEmpty()) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LinearProgressIndicator(
                                        progress = { scannerProgress },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = stringResource(R.string.shortcut_resolve_dialog_title))
                                } else {
                                    Text(
                                        text = stringResource(
                                            R.string.shortcut_on_address_found, address ?: ""
                                        )
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        8.dp, Alignment.CenterHorizontally
                                    ), modifier = Modifier.animateContentSize()
                                ) {
                                    if (!address.isNullOrEmpty()) {
                                        Button(onClick = { openUrl(address) }) {
                                            Text(text = stringResource(R.string.shortcut_resolve_dialog_browse_button_label))
                                        }
                                    }
                                    OutlinedButton(onClick = { stopActivity() }) {
                                        Text(text = stringResource(R.string.shortcut_resolve_dialog_cancel_button_label))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        customTabsHelper.bindCustomTabService()
    }

    private fun openUrl(address: String?) {
        if (!address.isNullOrEmpty()) {
            val url = UrlUtils.addressAsUrl(address)
            val browserChoice = intent.getStringExtra(ShortcutManager.KEY_PREFERRED_BROWSER)?.let {
                when (it) {
                    BrowserChoice.Default.packageName -> BrowserChoice.Default
                    BrowserChoice.AskUser.packageName -> BrowserChoice.AskUser
                    BrowserChoice.CustomTab.packageName -> BrowserChoice.CustomTab
                    else -> BrowserChoice.SavedBrowser(it)
                }
            }

            UrlUtils.browseUrl(context = this, url = url, browserChoice = browserChoice)
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onResume: closing activity")
        stopActivity()
    }

    private fun stopActivity() {
        if (this::customTabsHelper.isInitialized) {
            customTabsHelper.unbindCustomTabsService()
        }
        viewModel.terminateBackgroundProcess()
        finish()
    }

    companion object {
        private const val TAG = "ShortcutHandleActivity"
    }
}
