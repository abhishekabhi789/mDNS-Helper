package io.github.abhishekabhi789.mdnshelper.data

import android.graphics.Bitmap
import io.github.abhishekabhi789.mdnshelper.R

sealed class BrowserChoice {
    abstract val choiceLabel: Int
    abstract val packageName: String

    data object Default : BrowserChoice() {
        override val choiceLabel = R.string.browser_choice_default
        override val packageName = "default"
    }

    data object AskUser : BrowserChoice() {
        override val choiceLabel = R.string.browser_choice_always_ask
        override val packageName = "askUser"
    }

    data object CustomTab : BrowserChoice() {
        override val choiceLabel = R.string.browser_choice_custom_tab
        override val packageName = "customTab"
    }

    data class InstalledBrowser(
        override val choiceLabel: Int = 0,
        val appIcon: Bitmap,
        val appName:String,
        override val packageName: String
    ) : BrowserChoice()
}
