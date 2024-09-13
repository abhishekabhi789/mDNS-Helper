package io.github.abhishekabhi789.mdnshelper

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.abhishekabhi789.mdnshelper.utils.BookmarkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MdnsHelperViewModel @Inject constructor(
    private val dnssdHelper: DnsSdHelper,
    private val bookmarkManager: BookmarkManager
) : ViewModel() {

    private val _discoveryRunning = MutableStateFlow(false)
    val discoveryRunning: StateFlow<Boolean> = _discoveryRunning.asStateFlow()

    private val _availableServices = MutableStateFlow<List<MdnsInfo>>(emptyList())
    val availableServices: StateFlow<List<MdnsInfo>> = _availableServices.asStateFlow()

    init {
        dnssdHelper.onServiceFoundCallback = { bonjourService ->
            viewModelScope.launch {
                _availableServices.update { list ->
                    if (list.none { it.bonjourService == bonjourService }) {
                        Log.d(TAG, "onServiceFound: new service ${bonjourService.regType}")
                        val mdnsInfo = MdnsInfo(bonjourService).apply {
                            setBookmarkStatus(checkBookmarked(this))
                        }
                        list.toMutableList().apply { add(mdnsInfo) }
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

    fun addOrRemoveFromBookmark(info: MdnsInfo, add: Boolean) {
        viewModelScope.launch {
            bookmarkManager.let { if (add) it.addBookMark(info) else it.removeBookmark(info) }
            _availableServices.update { currentList ->
                val tempList = currentList.toMutableList()
                tempList.find { it == info }?.setBookmarkStatus(checkBookmarked(info))
                tempList
            }
        }
    }

    companion object {
        private const val TAG = "MdnsHelperViewModel"
        private const val SCANNER_TIMEOUT = 10_000L //30s
    }
}
