package io.github.abhishekabhi789.mdnshelper.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
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
    private val shortcutManager = context.getSystemService(ShortcutManager::class.java)

    private var pinnedShortcuts = mutableListOf<ShortcutInfoCompat>()

    private val icon: Icon = Icon.createWithResource(context, R.mipmap.ic_launcher_round)

    init {
        refreshShortcutList()
    }

    private fun refreshShortcutList() {
        pinnedShortcuts =
            ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED)
    }

    fun addPinnedShortcut(info: MdnsInfo, onComplete: (success: Boolean) -> Unit) {
        var success = false
        if (shortcutManager.isRequestPinShortcutSupported) {
            try {
                val extras = Bundle().apply {
                    putString(KEY_SERVICE_TYPE, info.getServiceType())
                    putString(KEY_SERVICE_NAME, info.getDomain())
                }
                val shortcutIntent = Intent(context, ShortcutHandleActivity::class.java).apply {
                    setAction(BuildConfig.SHORTCUT_ACTION_NAME)
                    putExtras(extras)
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                val shortcutId = info.getServiceType() + info.getServiceName()
                val shortcut = ShortcutInfo.Builder(context, shortcutId).apply {
                    setShortLabel(info.getServiceName())
                    setLongLabel("${info.getHostName()} from ${info.getServiceType()} ${info.getServiceName()}")
                    setIcon(icon)
                    setDisabledMessage("Failed to start shortcut, try recreating")
                    info.getHostAddress()?.let { setIntent(shortcutIntent) }
                }.build()
                shortcutManager.requestPinShortcut(shortcut, null)
                success = true
            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            } finally {
                onComplete(success)
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
            shortcutManager.disableShortcuts(shortcuts.map { it.id })
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
        return pinnedShortcuts.any { shortcutInfo -> isMatchingInfo(shortcutInfo, info) }
    }

    fun getShortcutInfoFromExtras(
        extras: Bundle,
        onFound: (regType: String, regName: String) -> Unit,
        onFailed: () -> Unit
    ) {
        val regType = extras.getString(KEY_SERVICE_TYPE)
        val regName = extras.getString(KEY_SERVICE_NAME)
        if (regType != null && regName != null) {
            onFound(regType, regName)
        } else onFailed()
    }

    private fun isMatchingInfo(shortcutInfo: ShortcutInfoCompat, mdnsInfo: MdnsInfo): Boolean {
        val shortcutExtra = shortcutInfo.extras
        val isTypeMatching = shortcutExtra?.getString(KEY_SERVICE_TYPE) == mdnsInfo.getServiceType()
        val isNameMatching = shortcutExtra?.getString(KEY_SERVICE_NAME) == mdnsInfo.getServiceName()
        return isTypeMatching && isNameMatching
    }

    companion object {
        private const val TAG = "ShortcutManager"
        private const val KEY_SERVICE_TYPE = "serviceType"
        private const val KEY_SERVICE_NAME = "serviceName"
    }
}
