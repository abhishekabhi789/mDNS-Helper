package io.github.abhishekabhi789.mdnshelper.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.abhishekabhi789.mdnshelper.ui.screens.SettingsScreen
import io.github.abhishekabhi789.mdnshelper.ui.theme.MDNSHelperTheme

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MDNSHelperTheme {
                SettingsScreen(onFinish = { finish() })
            }
        }
    }

    companion object {
        const val TAG = "SettingsActivity"
    }
}
