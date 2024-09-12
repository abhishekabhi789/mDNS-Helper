package io.github.abhishekabhi789.mdnshelper

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MdnsHelperViewModel @Inject constructor(private val nsdHelper: NsdHelper) : ViewModel() {

    private val _discoveryRunning = MutableStateFlow(false)
    val discoveryRunning: StateFlow<Boolean> = _discoveryRunning.asStateFlow()

    private val _availableServices = MutableStateFlow<List<MdnsInfo>>(emptyList())
    val availableServices: StateFlow<List<MdnsInfo>> = _availableServices.asStateFlow()

    init {
        nsdHelper.onServiceFoundCallback = { nsdInfo, resolverStatus ->
            viewModelScope.launch {
                _availableServices.update { list ->
                    val mdnsInfo = MdnsInfo(nsdInfo).apply {
                        this.updateResolverStatus(resolverStatus)
                    }
                    list.toMutableList().apply { add(mdnsInfo) }
                }
            }
        }

        nsdHelper.onServiceLostCallback = { service ->
            viewModelScope.launch {
                _availableServices.update { list ->
                    list.toMutableList()
                        .apply { removeAll { it.getServiceType() == service.serviceType && it.getServiceName() == service.serviceName } }
                }
            }
        }

        nsdHelper.onServiceResolvedCallback = { serviceInfo, resolverStatus ->
            viewModelScope.launch {
                _availableServices.update { infoList ->
                    infoList.map { info ->
                        if (info.nsdServiceInfo == serviceInfo) {
                            if (resolverStatus == MdnsInfo.ResolverStatus.RESOLVED){
                                serviceInfo.host.hostAddress?.let { info.setHostAddress(it) }
                                info.setPort(serviceInfo.port)
                                Log.d("TAG", "updated info ")
                            }
                            info.updateResolverStatus(resolverStatus)
                        }
                        info
                    }
                }
            }
        }
    }

    fun resolveServiceInfo(info: MdnsInfo) {
        nsdHelper.resolveInfo(info.nsdServiceInfo)
    }

    fun startServiceDiscovery() {
        viewModelScope.launch {
            nsdHelper.startDiscovery()
            _discoveryRunning.value = true
        }
    }

    fun stopServiceDiscovery() {
        viewModelScope.launch {
            nsdHelper.stopDiscovery()
            _discoveryRunning.value = false
        }
    }
}
