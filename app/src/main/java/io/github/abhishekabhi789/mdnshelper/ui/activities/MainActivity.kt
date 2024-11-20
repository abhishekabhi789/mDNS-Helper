package io.github.abhishekabhi789.mdnshelper.ui.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.abhishekabhi789.mdnshelper.BuildConfig
import io.github.abhishekabhi789.mdnshelper.ui.screens.AppMain
import io.github.abhishekabhi789.mdnshelper.ui.theme.MDNSHelperTheme
import io.github.abhishekabhi789.mdnshelper.viewmodel.MainActivityViewmodel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var shortcutAddedReceiver: ShortcutAddedReceiver? = null
    private val viewModel: MainActivityViewmodel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val hasNearbyPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        else true
        setContent {
            MDNSHelperTheme {
                var nearbyPermissionGranted: Boolean by remember {
                    mutableStateOf(hasNearbyPermission)
                }
                val permissionLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                        Log.i(TAG, "onCreate: permission granted - $isGranted")
                        Toast(this@MainActivity).run {
                            if (isGranted) setText("Permission Granted")
                            else setText("Permission denied for searching nearby devices")
                            duration = Toast.LENGTH_SHORT
                            show()
                        }
                        nearbyPermissionGranted = isGranted
                    }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!nearbyPermissionGranted) permissionLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
                    } else {
                        Log.i(TAG, "onCreate: no need to ask nearyby device permission")
                        nearbyPermissionGranted = true
                    }
                }
                LaunchedEffect(nearbyPermissionGranted) {
                    if (nearbyPermissionGranted) {
                        viewModel.startServiceDiscovery()
                        viewModel.refreshShortcutIconList(context = this@MainActivity)
                    } else Log.i(TAG, "onCreate: nearby permission is not granted")
                }
                Surface(Modifier.fillMaxSize()) {
                    AppMain(viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: destroying nsdHelper")
        viewModel.stopServiceDiscovery()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: starting listener")
        if (shortcutAddedReceiver == null) {
            shortcutAddedReceiver = ShortcutAddedReceiver()
        }
        val intentFilter = IntentFilter(BuildConfig.ACTION_SHORTCUT_ADDED_PINNED)
        ContextCompat.registerReceiver(
            this, shortcutAddedReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: unregistering")
        shortcutAddedReceiver?.let { unregisterReceiver(it) }
        shortcutAddedReceiver = null
    }

    inner class ShortcutAddedReceiver : BroadcastReceiver() {
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
