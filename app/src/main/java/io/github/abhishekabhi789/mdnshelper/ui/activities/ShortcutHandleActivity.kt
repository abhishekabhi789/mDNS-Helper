package io.github.abhishekabhi789.mdnshelper.ui.activities

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
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
import io.github.abhishekabhi789.mdnshelper.utils.UrlUtils
import io.github.abhishekabhi789.mdnshelper.viewmodel.ShortcutHandleViewmodel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShortcutHandleActivity @Inject constructor() : ComponentActivity() {
    private val viewModel: ShortcutHandleViewmodel by viewModels()
    private var customTabConnection: CustomTabsServiceConnection? = null

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
                                    Text(text = "Resolving device address")
                                } else {
                                    Text(text = "Address $address")

                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                        8.dp, Alignment.CenterHorizontally
                                    ), modifier = Modifier.animateContentSize()
                                ) {
                                    if (!address.isNullOrEmpty()) {
                                        Button(onClick = { openUrl(address) }) {
                                            Text(text = "Browse URL")
                                        }
                                    }
                                    OutlinedButton(onClick = { finish() }) {
                                        Text(text = "Cancel")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openUrl(address: String?) {
        if (!address.isNullOrEmpty()) {
            val url = UrlUtils.addressAsUrl(address)
            val chosenBrowser = intent.getStringExtra(ShortcutManager.KEY_PREFERRED_BROWSER)
            if (chosenBrowser == BrowserChoice.CustomTab.packageName) {
                openWithCustomTab(this, Uri.parse(url))
            } else
                UrlUtils.openWithBrowser(
                    context = this@ShortcutHandleActivity,
                    url,
                    chosenBrowser,
                )
        }
    }

    private fun openWithCustomTab(context: Context, uri: Uri) {
        val connection = object : CustomTabsServiceConnection() {
            override fun onServiceDisconnected(name: ComponentName?) {
                Log.i(TAG, "onServiceDisconnected: ")
                Toast.makeText(context, "tab closed", Toast.LENGTH_SHORT).show()
            }

            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                Log.i(TAG, "onCustomTabsServiceConnected: ")
                client.warmup(0)
                val session = client.newSession(CustomTabsCallback())
                session?.let {
                    it.mayLaunchUrl(uri, null, null)
                    val customTabIntent = CustomTabsIntent.Builder(session)
                        .build()
                    customTabIntent.launchUrl(context, uri)
                }
            }
        }
        customTabConnection = connection
        val packageName = CustomTabsClient.getPackageName(context, null)
        Log.d(TAG, "openWithCustomTab: packageName $packageName")
        packageName?.let {
            CustomTabsClient.bindCustomTabsService(context, packageName, connection)
        }

    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onResume: closing activity")
        customTabConnection?.let {
            Log.i(TAG, "onRestart: disconnecting customTab")
            unbindService(it)
            customTabConnection = null
        }
        viewModel.terminateBackgroundProcess()
        finish()
    }

    companion object {
        private const val TAG = "ShortcutHandleActivity"
    }
}
