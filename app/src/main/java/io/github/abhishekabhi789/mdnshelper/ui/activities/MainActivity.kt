package io.github.abhishekabhi789.mdnshelper.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.abhishekabhi789.mdnshelper.BuildConfig
import io.github.abhishekabhi789.mdnshelper.ui.screens.AppMain
import io.github.abhishekabhi789.mdnshelper.ui.theme.MDNSHelperTheme
import io.github.abhishekabhi789.mdnshelper.viewmodel.MainActivityViewmodel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var shortcutAddedReceiver: ShortcutAddedReceiver? = null
    private val viewModel: MainActivityViewmodel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MDNSHelperTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppMain(viewModel)
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.startServiceDiscovery()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: destroying nsdHelper")
        viewModel.stopServiceDiscovery()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: starting listener")
        if (shortcutAddedReceiver == null) {
            shortcutAddedReceiver = ShortcutAddedReceiver()
        }
        val intentFilter = IntentFilter(BuildConfig.ACTION_SHORTCUT_ADDED_PINNED)
        registerReceiver(shortcutAddedReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: unregistering")
        shortcutAddedReceiver?.let { unregisterReceiver(it) }
        shortcutAddedReceiver = null
    }

    inner class ShortcutAddedReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "onReceive: ${intent.action}")
            if (intent.action == BuildConfig.ACTION_SHORTCUT_ADDED_PINNED) {
                intent.let { viewModel.shortcutAddedEvent(it) }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
