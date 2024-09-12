package io.github.abhishekabhi789.mdnshelper

import android.net.nsd.NsdServiceInfo

data class MdnsInfo(val nsdServiceInfo: NsdServiceInfo) {
    enum class ResolverStatus { NOT_RESOLVED, RESOLVING, RESOLVED, FAILED }

    private var resolverStatus = ResolverStatus.NOT_RESOLVED
    private var hostAddress: String? = null
    private var port: Int? = null

    fun updateResolverStatus(newStatus: ResolverStatus) {
        this.resolverStatus = newStatus
    }

    fun setHostAddress(hostAddress: String) {
        this.hostAddress = hostAddress
    }

    fun setPort(port: Int) {
        this.port = port
    }

    fun getResolverStatus(): ResolverStatus = this.resolverStatus
    fun getServiceType(): String = nsdServiceInfo.serviceType
    fun getServiceName(): String = nsdServiceInfo.serviceName
    fun getHostAddress() = this.hostAddress
    fun getPort() = this.port
}
