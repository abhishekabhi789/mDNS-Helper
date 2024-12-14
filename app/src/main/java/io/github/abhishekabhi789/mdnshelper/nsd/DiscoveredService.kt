package io.github.abhishekabhi789.mdnshelper.nsd

import android.net.nsd.NsdServiceInfo

/** Class to carry the discovered service before resolving their info*/
sealed class DiscoveredService {
    abstract fun getType(): String
    abstract fun getName(): String?
    abstract fun getServiceDomain(): String?

    /** If RxDnsSd is used to discover, a BonjourService will be obtained and their [getType],[getName] and [getServiceDomain] are stored here for resolving.
     * This cannot be resolved with NsdManager*/
    data class BonjourInfo(
        val regType: String,
        val serviceName: String,
    ) :
        DiscoveredService() {
        private val regTypeParts = regType.split(SEPARATOR)
        private val serviceType = serviceName + SEPARATOR + regTypeParts.first()
        override fun getName(): String? = null
        override fun getType(): String = serviceType
        override fun getServiceDomain(): String =
            regType.removePrefix(regTypeParts.first() + SEPARATOR)
    }

    /**If NsdManager is used to discover, the [NsdServiceInfo] can be stored here.*/
    data class NsdInfo(val serviceInfo: NsdServiceInfo) : DiscoveredService() {
        override fun getType(): String = serviceInfo.serviceType
        override fun getName(): String = serviceInfo.serviceName
        override fun getServiceDomain(): String? = null
    }

    data class ShortcutInfo(
        val serviceType: String,
        val serviceName: String,
        val domain: String?
    ) : DiscoveredService() {
        override fun getType(): String = serviceType
        override fun getName(): String = serviceName
        override fun getServiceDomain(): String? = domain
    }

    companion object {
        const val SEPARATOR = "."
    }
}
