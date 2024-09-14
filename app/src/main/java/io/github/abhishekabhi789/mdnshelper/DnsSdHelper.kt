package io.github.abhishekabhi789.mdnshelper

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import com.github.druk.rx2dnssd.BonjourService
import com.github.druk.rx2dnssd.Rx2Dnssd
import com.github.druk.rx2dnssd.Rx2DnssdBindable
import com.github.druk.rx2dnssd.Rx2DnssdEmbedded
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsSdHelper @Inject constructor(@ApplicationContext context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var rxDnsSd = createDnsSd(context)
    private lateinit var browseDisposable: Disposable
    private var isResolverRunning = false
    var isDiscoveryRunning: ((Boolean) -> Unit)? = null
    var onServiceFoundCallback: ((BonjourService) -> Unit)? = null
    var onServiceLostCallback: ((serviceName: String) -> Unit)? = null
    private val serviceQueue: Queue<NsdServiceInfo> = LinkedList()

    private fun enqueueList(regType: NsdServiceInfo) {
        serviceQueue.offer(regType)
        processNextInQueue()
    }

    private fun processNextInQueue() {
        if (!isResolverRunning && serviceQueue.isNotEmpty()) {
            val nextRegType = serviceQueue.poll()
            Log.d(
                TAG,
                "processNextInQueue: processing ${nextRegType?.serviceName}, remaining ${serviceQueue.size}"
            )
            nextRegType?.let { resolveService(nextRegType) }
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e(TAG, "onStartDiscoveryFailed: error- $errorCode service type- $serviceType")
            isDiscoveryRunning?.invoke(false)
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.e(TAG, "onStopDiscoveryFailed: error- $errorCode service type- $serviceType")
            isDiscoveryRunning?.invoke(true)
        }

        override fun onDiscoveryStarted(serviceType: String?) {
            Log.i(TAG, "onDiscoveryStarted: service type- $serviceType")
            isDiscoveryRunning?.invoke(true)
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.i(TAG, "onDiscoveryStopped: service type- $serviceType")
            isDiscoveryRunning?.invoke(false)
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "onServiceFound: ${serviceInfo?.serviceName} ${serviceInfo?.serviceType}")
            serviceInfo?.let { enqueueList(serviceInfo) }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "onServiceLost: ${serviceInfo?.serviceName} ${serviceInfo?.serviceType}")
            serviceInfo?.let { onServiceLostCallback?.invoke(it.serviceName) }
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        val (name, domain) = serviceInfo.serviceType.split(".")
        val regType = "${serviceInfo.serviceName}.$name"
        try {
            browseDisposable = rxDnsSd.browse(regType, domain)
                .compose(rxDnsSd.resolve())
                .compose(rxDnsSd.queryIPRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bonjourService ->
                    if (bonjourService.isLost) {
                        Log.d(TAG, "resolveService: service lost ${bonjourService.serviceName}")
                        onServiceLostCallback?.invoke(bonjourService.serviceName)
                    } else {
                        Log.d(TAG, "resolveService: service found ${bonjourService.serviceName}")
                        onServiceFoundCallback?.invoke(bonjourService)
                    }
                }, { throwable -> Log.e(TAG, "resolveService: failed", throwable) })
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            processNextInQueue()
        }

    }

    fun startServiceDiscovery() {
        Log.d(TAG, "startServiceDiscovery: registering listener")
        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopServiceDiscovery() {
        Log.i(TAG, "stopServiceDiscovery: disposing browser and unregistering discoveryListener")
        if (this::browseDisposable.isInitialized) {
            if (!browseDisposable.isDisposed) {
                browseDisposable.dispose()
            }
        }
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDnsSd(context: Context): Rx2Dnssd {
        // https://developer.android.com/about/versions/12/behavior-changes-12#mdnsresponder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i(TAG, "Using embedded version of dns sd")
            return Rx2DnssdEmbedded(context)
        } else {
            Log.i(TAG, "Using bindable version of dns sd")
            return Rx2DnssdBindable(context)
        }
    }

    companion object {
        private const val TAG = "DnsSdHelper"
        private const val SERVICE_TYPE = "_services._dns-sd._udp"
    }
}
