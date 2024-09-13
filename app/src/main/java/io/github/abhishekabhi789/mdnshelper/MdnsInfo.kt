package io.github.abhishekabhi789.mdnshelper

import com.github.druk.rx2dnssd.BonjourService

data class MdnsInfo(val bonjourService: BonjourService) {

    private var bookmarked = false

    fun getServiceType(): String = bonjourService.regType

    fun getServiceName(): String = bonjourService.serviceName

    fun getHostName(): String? = bonjourService.hostname

    fun getHostAddress(): String? = bonjourService.inet4Address?.hostAddress

    fun isBookMarked(): Boolean = this.bookmarked

    fun setBookmarkStatus(isBookmarked: Boolean) {
        this.bookmarked = isBookmarked
    }
}
