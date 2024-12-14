package io.github.abhishekabhi789.mdnshelper.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.druk.rx2dnssd.BonjourService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.abhishekabhi789.mdnshelper.bookmarks.BookmarkManager
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.data.ServiceInfo
import io.github.abhishekabhi789.mdnshelper.nsd.ServiceDiscoveryManager
import io.github.abhishekabhi789.mdnshelper.shortcut.ShortcutManager
import io.github.abhishekabhi789.mdnshelper.utils.ShortcutIconUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainActivityViewmodel @Inject constructor(
    private val dnssdHelper: ServiceDiscoveryManager,
    private val bookmarkManager: BookmarkManager,
    private val shortcutManager: ShortcutManager?
) : ViewModel() {
    private val _discoveryRunning = MutableStateFlow(false)
    val discoveryRunning: StateFlow<Boolean> = _discoveryRunning.asStateFlow()

    private val _availableServices = MutableStateFlow<List<MdnsInfo>>(emptyList())
    val availableServices: StateFlow<List<MdnsInfo>> = _availableServices.asStateFlow()

    private val _shortcutResult = MutableSharedFlow<String>()
    val shortcutResult = _shortcutResult.asSharedFlow()

    private val _shortcutIcons = MutableStateFlow<List<Bitmap>>(emptyList())
    val shortcutIcons = _shortcutIcons.asStateFlow()

    private val bookmarks = bookmarkManager.bookmarks
    private var shortcutIconMap = emptyMap<Bitmap, String>()

    val unavailableBookmarks: StateFlow<List<MdnsInfo>> =
        combine(_availableServices, bookmarks) { availableServices, bookmarks ->
            bookmarks.toMutableSet().filter { bookmark ->
                availableServices.none { info ->
                    Pair(info.getServiceType(), info.getServiceName()) == bookmark
                }
            }.map { bookmarkToServiceInfo(it).apply { this.setBookmarkStatus(true) } }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    init {
        dnssdHelper.onServiceFoundCallback = { serviceInfo ->
            viewModelScope.launch {
                _availableServices.update { list ->
                    if (list.none { it.service == serviceInfo.service }) {
                        Log.d(TAG, "onServiceFound: new service $serviceInfo")
                        serviceInfo.setBookmarkStatus(checkBookmarked(serviceInfo))
                        list.toMutableList().apply { add(serviceInfo) }
                    } else list
                }
            }
        }

        dnssdHelper.onServiceLostCallback = { serviceName ->
            viewModelScope.launch {
                _availableServices.update { list ->
                    list.toMutableList()
                        .apply { removeAll { it.getServiceName() == serviceName } }
                }
            }
        }
    }

    private fun checkBookmarked(info: MdnsInfo): Boolean {
        return bookmarkManager.isBookmarked(info)
    }

    fun startServiceDiscovery() {
        viewModelScope.launch {
            Log.d(TAG, "startServiceDiscovery: starting with timeout $SCANNER_TIMEOUT")
            dnssdHelper.startServiceDiscovery()
            _discoveryRunning.update { true }
            launch {
                delay(SCANNER_TIMEOUT)
                Log.d(TAG, "startServiceDiscovery: scan timeout")
                stopServiceDiscovery()
            }
        }
    }

    fun stopServiceDiscovery() {
        viewModelScope.launch {
            dnssdHelper.stopServiceDiscovery()
            _discoveryRunning.update { false }
        }
    }

    fun addOrRemoveFromBookmark(
        info: MdnsInfo,
        add: Boolean,
        onComplete: (success: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            bookmarkManager.let {
                val success = if (add) it.addBookMark(info) else it.removeBookmark(info)
                onComplete(success)
            }
            _availableServices.update { currentList ->
                val tempList = currentList.toMutableList()
                tempList.find { it == info }?.setBookmarkStatus(checkBookmarked(info))
                tempList
            }
        }
    }

    fun addPinnedShortcut(info: MdnsInfo, iconBitmap: Bitmap, preferredBrowser: BrowserChoice) {
        viewModelScope.launch {
            shortcutManager?.addPinnedShortcut(info, iconBitmap, preferredBrowser)
        }
    }

    suspend fun isShortcutAdded(info: MdnsInfo): Boolean {
        return withContext(Dispatchers.IO) {
            shortcutManager?.isShortcutAdded(info) ?: false
        }
    }

    fun shortcutAddedEvent(intent: Intent) {
        viewModelScope.launch {
            shortcutManager?.getShortcutInfoFromIntent(intent,
                onFound = { _, regName, _ -> launch { _shortcutResult.emit(regName) } },
                onFailed = {
                    Log.e(TAG, "shortcutAddedEvent: failed to get shortcut info")
                })
        }
    }

    private fun bookmarkToServiceInfo(bookmarkInfo: Pair<String, String>): MdnsInfo {
        val bonjourService = BonjourService.Builder(
            BonjourService.LOST,
            (Math.random() * 100).toInt(),
            bookmarkInfo.second,
            bookmarkInfo.first,
            ServiceDiscoveryManager.LOCAL_DOMAIN
        ).build()
        return MdnsInfo(ServiceInfo.BonjourInfo(bonjourService))
    }

    fun refreshShortcutIconList(context: Context) {
        viewModelScope.launch {
            _shortcutIcons.update {
                val iconsMap = ShortcutIconUtils.getSavedIcons(context)
                Log.d(TAG, "refreshShortcutIconList: ${it.size}")
                shortcutIconMap = iconsMap
                iconsMap.keys.toList()
            }
        }
    }

    fun deleteIcon(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            val fileName = shortcutIconMap[bitmap]
            fileName?.let {
                ShortcutIconUtils.deleteIcon(
                    context, fileName,
                    onComplete = { refreshShortcutIconList(context) })
            }
        }
    }

    companion object {
        private const val TAG = "MdnsHelperViewModel"
        private const val SCANNER_TIMEOUT = 10_000L
    }
}
