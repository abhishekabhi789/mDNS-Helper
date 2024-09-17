package io.github.abhishekabhi789.mdnshelper.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object UrlUtils {

    fun browseUrl(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun shareUrl(context: Context, url: String) {
        context.startActivity(Intent(Intent.ACTION_SEND, Uri.parse(url)))
    }

    fun addressAsUrl(address: String): String {
        return "http://$address"
    }

    fun openWithBrowser(context: Context, url: String) {
        val parsedUri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, parsedUri)
        context.startActivity(intent)

    }
}
