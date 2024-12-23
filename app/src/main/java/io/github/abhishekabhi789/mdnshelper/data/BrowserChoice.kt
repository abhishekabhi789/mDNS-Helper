package io.github.abhishekabhi789.mdnshelper.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import io.github.abhishekabhi789.mdnshelper.R

sealed class BrowserChoice {
    open val choiceLabel: Int = 0
    abstract val packageName: String
    open fun getLabel(context: Context): String = context.getString(this.choiceLabel)


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
        val appIcon: Bitmap,
        val appName: String,
        override val packageName: String
    ) : BrowserChoice() {
        override fun getLabel(context: Context): String = appName

    }

    data class SavedBrowser(override val packageName: String) : BrowserChoice() {
        override fun getLabel(context: Context): String {
            return try {
                context.packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.MATCH_UNINSTALLED_PACKAGES
                ).let {
                    context.packageManager.getApplicationLabel(it).toString()
                }
            } catch (e: Exception) {
                Log.e(TAG, "getLabel: failed to get preferred browser name, $packageName", e)
                context.getString(R.string.unknown)
            }
        }
    }

    companion object {
        private const val TAG = "BrowserChoice"
    }
}
