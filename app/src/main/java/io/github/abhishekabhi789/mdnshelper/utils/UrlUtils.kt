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

    fun addressAsUrl(url: String): String {
        return "http://$url"
    }
}
