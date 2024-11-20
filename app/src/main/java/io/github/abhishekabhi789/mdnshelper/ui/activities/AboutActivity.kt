package io.github.abhishekabhi789.mdnshelper.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import io.github.abhishekabhi789.mdnshelper.ui.screens.AboutScreen
import io.github.abhishekabhi789.mdnshelper.ui.theme.MDNSHelperTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MDNSHelperTheme {
                AboutScreen(modifier = Modifier) {
                    finish()
                }
            }
        }
    }
}
