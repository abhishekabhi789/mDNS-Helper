package io.github.abhishekabhi789.mdnshelper.shortcut

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.abhishekabhi789.mdnshelper.BuildConfig
import io.github.abhishekabhi789.mdnshelper.R
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.ui.activities.ShortcutHandleActivity
import javax.inject.Inject
import javax.inject.Singleton

@RequiresApi(Build.VERSION_CODES.O)
@Singleton
class ShortcutManager @Inject constructor(@ApplicationContext private val context: Context) {

    private var pinnedShortcuts = mutableListOf<ShortcutInfoCompat>()

    private val icon = IconCompat.createWithResource(context, R.mipmap.ic_launcher_round)

    init {
        refreshShortcutList()
    }

    private fun refreshShortcutList() {
        pinnedShortcuts =
            ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED)
        Log.d(
            TAG,
            "refreshShortcutList: refreshing pinned shortcut list ," +
                    " size ${pinnedShortcuts.size} " +
                    pinnedShortcuts.joinToString(",") { it.shortLabel }
        )
    }

    fun addPinnedShortcut(info: MdnsInfo) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            val extras = PersistableBundle().apply {
                putString(KEY_SERVICE_TYPE, info.getServiceType())
                putString(KEY_SERVICE_NAME, info.getServiceName())
                putString(KEY_SERVICE_DOMAIN, info.getDomain())
            }
            try {
                val shortcutIntent = Intent(context, ShortcutHandleActivity::class.java).apply {
                    setAction(BuildConfig.ACTION_SHORTCUT_LAUNCH)
                    putExtras(Bundle(extras))
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                val shortcutId = info.getServiceType() + info.getServiceName()
                val shortcut = ShortcutInfoCompat.Builder(context, shortcutId).apply {
                    setShortLabel(info.getServiceName().replaceFirstChar { it.uppercase() })
                    setLongLabel("${info.getHostName()} from ${info.getServiceType()} ${info.getServiceName()}")
                    setIcon(icon)
                    setExtras(extras)
                    setDisabledMessage("Failed to start shortcut, try recreating")
                    info.getHostAddress()?.let { setIntent(shortcutIntent) }
                }.build()
                val callbackIntent = Intent(BuildConfig.ACTION_SHORTCUT_ADDED_PINNED).apply {
                    setPackage(context.packageName)
                    putExtras(Bundle(extras))
                }
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    shortcut.hashCode(), callbackIntent, PendingIntent.FLAG_IMMUTABLE
                )
                ShortcutManagerCompat.requestPinShortcut(
                    context, shortcut, successCallback.intentSender,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                refreshShortcutList()
            }
        }
    }

    fun disablePinnedShortcut(info: MdnsInfo, onComplete: (success: Boolean) -> Unit) {
        var success = false
        try {
            val shortcuts = pinnedShortcuts.filter { shortcutInfo ->
                isMatchingInfo(shortcutInfo, info)
            }
            Log.d(TAG, "removePinnedShortcut: found ${shortcuts.size} matches")
            ShortcutManagerCompat.disableShortcuts(
                context,
                shortcuts.map { it.id },
                "Shortcut removed, try recreating"
            )
            success = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            success = false
        } finally {
            onComplete(success)
            refreshShortcutList()
        }
    }

    fun isShortcutAdded(info: MdnsInfo): Boolean {
        refreshShortcutList()
        val isFound = pinnedShortcuts.any { shortcutInfo -> isMatchingInfo(shortcutInfo, info) }
        Log.i(TAG, "isShortcutAdded: ${info.getServiceName()} - $isFound")
        return isFound
    }

    fun getShortcutInfoFromIntent(
        intent: Intent,
        onFound: (regType: String, regName: String, domain: String?) -> Unit,
        onFailed: () -> Unit
    ) {
        val regType = intent.getStringExtra(KEY_SERVICE_TYPE)
        val regName = intent.getStringExtra(KEY_SERVICE_NAME)
        val domain = intent.getStringExtra(KEY_SERVICE_DOMAIN)
        if (regType != null && regName != null) {
            Log.i(TAG, "getShortcutInfoFromExtras: $regType, $regName")
            onFound(regType, regName, domain)
        } else {
            Log.e(
                TAG,
                "getShortcutInfoFromExtras: " +
                        "failed to get get one of these $regType, $regName, $domain"
            )
            onFailed()
        }
    }

    private fun isMatchingInfo(shortcutInfo: ShortcutInfoCompat, mdnsInfo: MdnsInfo): Boolean {
        val shortcutExtra = shortcutInfo.extras
        Log.d(TAG, "isMatchingInfo: ${shortcutExtra.toString()}")
        val isTypeMatching = shortcutExtra?.getString(KEY_SERVICE_TYPE) == mdnsInfo.getServiceType()
        val isNameMatching = shortcutExtra?.getString(KEY_SERVICE_NAME) == mdnsInfo.getServiceName()
        return isTypeMatching && isNameMatching
    }

    companion object {
        private const val TAG = "ShortcutManager"
        private const val KEY_SERVICE_TYPE = "service_type"
        private const val KEY_SERVICE_NAME = "service_name"
        private const val KEY_SERVICE_DOMAIN = "service_domain"
    }
}
