package io.github.abhishekabhi789.mdnshelper.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.abhishekabhi789.mdnshelper.nsd.ServiceDiscoveryManager
import io.github.abhishekabhi789.mdnshelper.shortcut.ShortcutManager
import kotlinx.coroutines.delay
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
    private val _resolvingProgress = MutableStateFlow(0f)
    var resolvingProgress = _resolvingProgress.asStateFlow()
    private var isResolverRunning = false

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
        dnssdHelper.isDiscoveryRunning = { isRunning ->
            isResolverRunning = isRunning
        }
    }

    fun processShortcutAction(intent: Intent) {
        viewModelScope.launch {
            updateProgress(0f)
            launch {
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
            launch {
                val endTime = System.currentTimeMillis() + TIMEOUT
                while (System.currentTimeMillis() < endTime) {
                    val progress = 1f - ((endTime - System.currentTimeMillis()) / TIMEOUT.toFloat())
                    updateProgress(progress)
                    delay(50)
                }
                Log.e(TAG, "processShortcutAction: timeout")
                sendErrorMessage("Failed to find service.")
            }
        }
    }

    private fun sendErrorMessage(errMsg: String) {
        viewModelScope.launch {
            _errorMessage.emit(errMsg)
        }
    }

    private fun updateProgress(progress: Float) {
        viewModelScope.launch {
            _resolvingProgress.update { progress.coerceIn(0f, 1.0f) }
        }
    }

    fun terminateBackgroundProcess() {
        viewModelScope.launch {
            dnssdHelper.stopServiceDiscovery()
        }
    }

    companion object {
        private const val TAG = "ShortcutHandleViewmodel"
        private const val TIMEOUT = 15_000L
    }
}
