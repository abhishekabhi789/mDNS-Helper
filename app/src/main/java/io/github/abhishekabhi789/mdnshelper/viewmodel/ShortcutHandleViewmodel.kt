package io.github.abhishekabhi789.mdnshelper.viewmodel

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.abhishekabhi789.mdnshelper.nsd.ServiceDiscoveryManager
import io.github.abhishekabhi789.mdnshelper.shortcut.ShortcutManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortcutHandleViewmodel @Inject constructor(
    private val dnssdHelper: ServiceDiscoveryManager,
    private val shortcutManager: ShortcutManager?
) : ViewModel() {
    private var _currentAddress = MutableStateFlow<String?>(null)
    val currentAddress = _currentAddress.asStateFlow()
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        dnssdHelper.onServiceFoundCallback = { bonjourService ->
            val address = bonjourService.inet4Address?.hostAddress
            Log.i(TAG, "onServiceFoundCallback: address: $address")
            _currentAddress.update { address }
        }
        dnssdHelper.onServiceLostCallback = { serviceName ->
            Log.i(TAG, "onServiceLostCallback: $serviceName lost")
            sendErrorMessage("Service $serviceName lost")
        }
        dnssdHelper.onError = { errorMsg -> sendErrorMessage(errorMsg) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun processShortcutAction(intent: Intent) {
        shortcutManager?.getShortcutInfoFromIntent(
            intent,
            onFound = { regType, _, domain ->
                dnssdHelper.resolveServiceWithInfos(regType, domain)
            },
            onFailed = {
                Log.e(TAG, "isServiceAvailable: failed to get shortcut info")
                sendErrorMessage("Failed to retrieve shortcut info")
            })
    }

    private fun sendErrorMessage(errMsg: String) {
        viewModelScope.launch {
            _errorMessage.emit(errMsg)
        }
    }

    fun terminateBackgroundProcess() {
        viewModelScope.launch {
            dnssdHelper.stopServiceDiscovery()
        }
    }

    companion object {
        private const val TAG = "ShortcutHandleViewmodel"
    }
}
