package io.github.abhishekabhi789.mdnshelper

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MdnsHelperViewModel @Inject constructor(private val dnssdHelper: DnsSdHelper) : ViewModel() {

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
                        list.toMutableList().apply { add(MdnsInfo(bonjourService)) }
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

    companion object {
        private const val TAG = "MdnsHelperViewModel"
        private const val SCANNER_TIMEOUT = 10_000L //30s
    }
}
