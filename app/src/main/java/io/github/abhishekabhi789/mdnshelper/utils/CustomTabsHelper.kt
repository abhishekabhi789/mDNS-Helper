package io.github.abhishekabhi789.mdnshelper.utils

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomTabsHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private var customTabConnection: CustomTabsServiceConnection? = null
    private var customTabSession: CustomTabsSession? = null

    fun bindCustomTabService() {
        val connection = object : CustomTabsServiceConnection() {
            override fun onServiceDisconnected(name: ComponentName?) {
                Log.i(TAG, "bindCustomTabService-onServiceDisconnected: $name")
                customTabSession = null
            }

            override fun onCustomTabsServiceConnected(
                name: ComponentName,
                client: CustomTabsClient
            ) {
                Log.i(TAG, "bindCustomTabService-onCustomTabsServiceConnected: $name")
                client.warmup(0)
                customTabSession = client.newSession(null)
            }
        }
        customTabConnection = connection
        val packageName = CustomTabsClient.getPackageName(context, null)
        Log.i(TAG, "bindCustomTabService: packageName $packageName")
        packageName?.let {
            CustomTabsClient.bindCustomTabsService(context, packageName, connection)
        }
    }

    fun unbindCustomTabsService() {
        Log.d(TAG, "unbindCustomTabsService: unbinding")
        customTabConnection?.let { context.unbindService(it) }
        customTabConnection = null
        customTabSession = null
        Log.i(TAG, "unbindCustomTabsService: session removed")
    }

    fun browseURL(context: Context, url: String) {
        Log.d(TAG, "browseURL: url $url")
        val uri = Uri.parse(url)
        customTabSession?.let { session ->
            session.mayLaunchUrl(uri, null, null)
            CustomTabsIntent.Builder(session).build().launchUrl(context, uri)
        } ?: Log.e(TAG, "browseURL: custom tab session not initialized")
    }

    companion object {
        private const val TAG = "CustomTabHelper"
    }
}
