package io.github.abhishekabhi789.mdnshelper.data

import android.graphics.Bitmap

sealed class BrowserChoice {
    abstract val name: String
    abstract val packageName: String

    data object Default : BrowserChoice() {
        override val name = "Default"
        override val packageName = "default"
    }

    data object AskUser : BrowserChoice() {
        override val name = "Ask Everytime"
        override val packageName = "askUser"
    }

    data object CustomTab : BrowserChoice() {
        override val name = "Custom Tab"
        override val packageName = "customTab"
    }

    data class InstalledBrowser(
        override val name: String,
        val appIcon: Bitmap,
        override val packageName: String
    ) : BrowserChoice()
}
