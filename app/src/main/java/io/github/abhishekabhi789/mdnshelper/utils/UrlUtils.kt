package io.github.abhishekabhi789.mdnshelper.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import io.github.abhishekabhi789.mdnshelper.MdnsHelperApplication
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice.InstalledBrowser

object UrlUtils {
    private const val TAG = "UrlUtils"
    private const val HTTP_PREFIX = "http://"

    fun shareUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                setType("text/plain")
                putExtra(Intent.EXTRA_TEXT, url)
            }
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.chooser_dialog_title_share_action, url)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "browseUrl: failed to share url with", e)
        }
    }

    fun addressAsUrl(address: String): String {
        return "$HTTP_PREFIX$address"
    }

    /**@param packageName pass null to show chooser*/
    private fun openWithBrowserSpecificBrowser(
        context: Context,
        url: String,
        packageName: String?
    ) {
        Log.d(TAG, "openWithBrowserSpecificBrowser: packageName: $packageName url :$url")
        val parsedUri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, parsedUri)
        if (packageName.isNullOrEmpty()) {
            Log.i(TAG, "openWithBrowserSpecificBrowser: showing chooser")
            Intent.createChooser(
                intent,
                context.getString(R.string.chooser_dialog_title_browse_action, url)
            ).run {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                context.startActivity(this)
            }
        } else {
            try {
                context.startActivity(intent.setPackage(packageName))
            } catch (e: Exception) {
                Log.e(TAG, "openWithBrowserSpecificBrowser: failed $packageName", e)
            }
        }
    }

    fun getBrowsers(context: Context): List<InstalledBrowser> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(HTTP_PREFIX)).apply {
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfos.map { resolveInfo ->
            InstalledBrowser(
                appIcon = resolveInfo.loadIcon(pm).toBitmap(config = Bitmap.Config.ARGB_8888),
                appName = resolveInfo.loadLabel(pm).toString(),
                packageName = resolveInfo.activityInfo.packageName
            )
        }
    }

    private fun openWithDefaultBrowser(context: Context, url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Log.e(TAG, "browseUrl: failed to browse url $url", e)
        }
    }

    fun browseUrl(context: Context, url: String?, browserChoice: BrowserChoice? = null) {
        Log.d(TAG, "browseUrl: $url")
        if (!url.isNullOrEmpty()) {
            val application = (context.applicationContext as MdnsHelperApplication)
            val chosenBrowser = browserChoice ?: application.preferredBrowserChoice
            Log.i(TAG, "browseUrl: chosen browser method $chosenBrowser")
            when (chosenBrowser) {
                BrowserChoice.CustomTab -> {
                    application.customTabsHelper.browseURL(context, url)
                }

                BrowserChoice.AskUser -> openWithBrowserSpecificBrowser(context, url, null)
                BrowserChoice.Default -> openWithDefaultBrowser(context, url)
                else -> {
                    openWithBrowserSpecificBrowser(
                        context = context,
                        url = url,
                        packageName = chosenBrowser.packageName,
                    )
                }
            }
        }
    }
}
