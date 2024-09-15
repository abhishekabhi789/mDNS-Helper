package io.github.abhishekabhi789.mdnshelper.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.abhishekabhi789.mdnshelper.ui.screens.AppMain
import io.github.abhishekabhi789.mdnshelper.ui.theme.MDNSHelperTheme
import io.github.abhishekabhi789.mdnshelper.viewmodel.MdnsHelperViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MdnsHelperViewModel by viewModels()

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
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.startServiceDiscovery()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: destroying nsdHelper")
        viewModel.stopServiceDiscovery()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
