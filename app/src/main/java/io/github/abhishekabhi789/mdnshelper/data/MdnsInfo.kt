package io.github.abhishekabhi789.mdnshelper.data

import android.net.nsd.NsdServiceInfo
import com.github.druk.rx2dnssd.BonjourService

sealed class ServiceInfo {
    data class BonjourInfo(val bonjourInfo: BonjourService) : ServiceInfo()
    data class NsdInfo(val nsdInfo: NsdServiceInfo) : ServiceInfo()
}

data class MdnsInfo(val service: ServiceInfo) {

    var isBookmarked = false
        private set

    fun getServiceType(): String = when (service) {
        is ServiceInfo.BonjourInfo -> service.bonjourInfo.regType
        is ServiceInfo.NsdInfo -> service.nsdInfo.serviceType
    }

    fun getServiceName(): String = when (service) {
        is ServiceInfo.BonjourInfo -> service.bonjourInfo.serviceName
        is ServiceInfo.NsdInfo -> service.nsdInfo.serviceName
    }

    fun getDomain(): String? = when (service) {
        is ServiceInfo.BonjourInfo -> service.bonjourInfo.domain
        is ServiceInfo.NsdInfo -> ""//not needed
    }

    fun getHostName(): String? = when (service) {
        is ServiceInfo.BonjourInfo -> service.bonjourInfo.hostname
        is ServiceInfo.NsdInfo -> "hostName to be implemented"
    }

    fun getHostAddress(): String? = when (service) {
        is ServiceInfo.BonjourInfo -> service.bonjourInfo.inet4Address?.hostAddress
        is ServiceInfo.NsdInfo -> "Host address to be implemented"
    }

    fun getPort(): String = when (service) {
        is ServiceInfo.BonjourInfo -> service.bonjourInfo.port.toString()
        is ServiceInfo.NsdInfo -> service.nsdInfo.port.toString()
    }

    fun getExtraInfo(): Map<String, String> = when (service) {
        is ServiceInfo.BonjourInfo -> service.bonjourInfo.txtRecords
        is ServiceInfo.NsdInfo -> emptyMap()
    }

    fun setBookmarkStatus(isBookmarked: Boolean) {
        this.isBookmarked = isBookmarked
    }
}
