package io.github.abhishekabhi789.mdnshelper.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice.InstalledBrowser

object UrlUtils {
    private const val TAG = "UrlUtils"
    private const val HTTP_PREFIX = "http://"
    fun browseUrl(context: Context, url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Log.e(TAG, "browseUrl: failed to browse url $url", e)
        }
    }

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

    fun openWithBrowser(context: Context, url: String, browserPackageName: String?) {
        val parsedUri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, parsedUri)
        when (browserPackageName) {
            BrowserChoice.Default.packageName -> browseUrl(context, url)
            BrowserChoice.AskUser.packageName -> {
                Log.d(TAG, "openWithBrowser: creating chooser")
                val chooserIntent = Intent
                    .createChooser(
                        intent,
                        context.getString(R.string.chooser_dialog_title_browse_action, url)
                    )
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                context.startActivity(chooserIntent)
            }

            else -> {
                if (!browserPackageName.isNullOrEmpty()) intent.setPackage(browserPackageName)
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(
                        TAG, "openWithBrowser: failed to open with ," +
                                " app: $browserPackageName, url: $url", e
                    )
                }
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
}
