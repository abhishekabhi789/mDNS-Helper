package io.github.abhishekabhi789.mdnshelper

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NsdHelper @Inject constructor(@ApplicationContext context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var isDiscoveryRunning = false
    private var isResolverRunning = false
    private val serviceQueue: Queue<NsdServiceInfo> = LinkedList()

    private fun enqueueList(serviceInfo: NsdServiceInfo) {
        serviceQueue.offer(serviceInfo)
        processNextInQueue()
    }

    private fun processNextInQueue() {
        if (!isResolverRunning && serviceQueue.isNotEmpty()) {
            val nextService = serviceQueue.poll()
            nextService?.let { resolveInfo(it) }
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e(TAG, "onStartDiscoveryFailed: error- $errorCode service type- $serviceType")
            isDiscoveryRunning = false
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e(TAG, "onStopDiscoveryFailed: error- $errorCode service type- $serviceType")
            isDiscoveryRunning = true
        }

        override fun onDiscoveryStarted(serviceType: String?) {
            Log.i(TAG, "onDiscoveryStarted: service type- $serviceType")
            isDiscoveryRunning = true
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.i(TAG, "onDiscoveryStopped: service type- $serviceType")
            isDiscoveryRunning = false
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "onServiceFound: ${serviceInfo?.serviceName} ${serviceInfo?.serviceType}")
            serviceInfo?.let {
                onServiceFoundCallback?.invoke(it, MdnsInfo.ResolverStatus.NOT_RESOLVED)
                enqueueList(serviceInfo)
            }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "onServiceLost: ${serviceInfo?.serviceName} ${serviceInfo?.serviceType}")
            serviceInfo?.let {
                onServiceLostCallback?.invoke(it)
            }
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed: error- $errorCode serviceInfo- $serviceInfo")
            isResolverRunning = false
            onServiceResolvedCallback?.invoke(serviceInfo, MdnsInfo.ResolverStatus.FAILED)
            processNextInQueue()
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")
            onServiceResolvedCallback?.invoke(serviceInfo, MdnsInfo.ResolverStatus.RESOLVED)
            isResolverRunning = false
            processNextInQueue()
        }
    }

    var onServiceFoundCallback: ((NsdServiceInfo, MdnsInfo.ResolverStatus) -> Unit)? = null
    var onServiceLostCallback: ((NsdServiceInfo) -> Unit)? = null
    var onServiceResolvedCallback: ((NsdServiceInfo, MdnsInfo.ResolverStatus) -> Unit)? = null

    fun startDiscovery() {
        if (!isDiscoveryRunning) {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }
    }

    fun stopDiscovery() {
        if (isDiscoveryRunning) {
            nsdManager.stopServiceDiscovery(discoveryListener)
        }
        stopResolution()
    }


    fun resolveInfo(serviceInfo: NsdServiceInfo) {
        nsdManager.resolveService(serviceInfo, resolveListener)
        onServiceResolvedCallback?.invoke(serviceInfo,MdnsInfo.ResolverStatus.RESOLVING)
        isResolverRunning = true
    }

    private fun stopResolution() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            nsdManager.stopServiceResolution(resolveListener)
            isResolverRunning = false
        }

    }

    companion object {
        private const val TAG = "NsdHelper"
        private const val SERVICE_TYPE = "_services._dns-sd._udp"
    }
}
