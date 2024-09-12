package io.github.abhishekabhi789.mdnshelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.abhishekabhi789.mdnshelper.ui.AppMain
import io.github.abhishekabhi789.mdnshelper.ui.theme.MDNSHelperTheme

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
        lifecycleScope.launchWhenCreated {
            viewModel.startServiceDiscovery()
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
