package io.github.abhishekabhi789.mdnshelper.nsd

import android.annotation.SuppressLint
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.ext.SdkExtensions
import android.util.Log
import com.github.druk.rx2dnssd.Rx2Dnssd
import com.github.druk.rx2dnssd.Rx2DnssdBindable
import com.github.druk.rx2dnssd.Rx2DnssdEmbedded
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo
import io.github.abhishekabhi789.mdnshelper.data.ServiceInfo
import io.github.abhishekabhi789.mdnshelper.utils.AppPreferences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceDiscoveryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
) {
    private val isServiceInfoCallbackSupported = isServiceInfoCallbackMethodAvailable()
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val multicastLock = wifiManager.createMulticastLock(TAG)
    private var rxDnsSd = createDnsSd(context)
    private lateinit var discoveryDisposable: Disposable
    private lateinit var resolverDisposable: Disposable
    private var isResolverRunning = false
    var isDiscoveryRunning: ((Boolean) -> Unit)? = null
    var onServiceFoundCallback: ((MdnsInfo) -> Unit)? = null
    var onServiceLostCallback: ((serviceName: String) -> Unit)? = null
    var onError: ((errorMsg: String) -> Unit)? = null
    private val serviceQueue: Queue<DiscoveredService> = LinkedList()
    private var discoverMethod: DiscoverMethod = DiscoverMethod.NsdManager
    private var resolvingMethod: ResolvingMethod = ResolvingMethod.NsdManager

    init {
        multicastLock.setReferenceCounted(true)
        CoroutineScope(Dispatchers.IO).launch {
            appPreferences.discoveryMethod.collect { method ->
                discoverMethod = method
                Log.d(TAG, "init: Updated discovery method: $discoverMethod")
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            appPreferences.resolvingMethod.collect { method ->
                resolvingMethod = method
                Log.d(TAG, "init: Updated resolving method: $resolvingMethod")
            }
        }
    }

    private fun enqueueList(service: DiscoveredService) {
        serviceQueue.offer(service)
        processNextInQueue()
    }

    private fun processNextInQueue() {
        if (!isResolverRunning && serviceQueue.isNotEmpty()) {
            val nextRegType = serviceQueue.poll()
            Log.d(
                TAG,
                "processNextInQueue: processing ${nextRegType}, remaining ${serviceQueue.size}"
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

        override fun onServiceFound(service: NsdServiceInfo?) {
            Log.i(TAG, "onServiceFound: ${service?.serviceName} ${service?.serviceType}")
            service?.let { enqueueList(DiscoveredService.NsdInfo(service)) }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Log.i(TAG, "onServiceLost: ${serviceInfo?.serviceName} ${serviceInfo?.serviceType}")
            serviceInfo?.let { onServiceLostCallback?.invoke(it.serviceName) }
        }
    }

    private val resolverListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.i(TAG, "onResolveFailed: errorCode: $errorCode $serviceInfo")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            serviceInfo?.let {
                val service = ServiceInfo.NsdInfo(it)
                onServiceFoundCallback?.invoke(MdnsInfo(service))
            }
        }
    }

    private val serviceInfoCallback =
        if (isServiceInfoCallbackSupported)
            @SuppressLint("NewApi")// RequiredExtension annotation causes crash
            object : NsdManager.ServiceInfoCallback {
                override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                    Log.e(TAG, "onServiceInfoCallbackRegistrationFailed: errorCode - $errorCode")
                }

                override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
                    val service = ServiceInfo.NsdInfo(serviceInfo)
                    onServiceFoundCallback?.invoke(MdnsInfo(service))
                }

                override fun onServiceLost() {
                    Log.i(TAG, "onServiceLost: ")
                }

                override fun onServiceInfoCallbackUnregistered() {
                    Log.i(TAG, "onServiceInfoCallbackUnregistered: ")
                }
            } else null

    private fun discoverWithDnsSd(
        serviceType: String = SERVICE_TYPE,
        domain: String = LOCAL_DOMAIN
    ) {
        isDiscoveryRunning?.invoke(true)
        resolverDisposable = rxDnsSd.browse(serviceType, domain)
            .subscribeOn(Schedulers.io())
            .subscribe({ bonjourService ->
                if (bonjourService.isLost) {
                    Log.d(TAG, "discoverWithDnsSd: service lost ${bonjourService.serviceName}")
                    onServiceLostCallback?.invoke(bonjourService.serviceName)
                } else {
                    Log.d(TAG, "discoverWithDnsSd: service found ${bonjourService.serviceName}")
                    val serviceInfo = bonjourService.run {
                        DiscoveredService.BonjourInfo(regType, serviceName)
                    }
                    bonjourService?.let { enqueueList(serviceInfo) }
                }
            }, { throwable ->
                Log.e(TAG, "discoverWithDnsSd: failed", throwable)
                isDiscoveryRunning?.invoke(false)
            }, {
                Log.d(TAG, "discoverWithDnsSd: completed")
                isDiscoveryRunning?.invoke(false)
            })
    }

    private fun resolveServiceWithDnsSd(regType: String, domain: String?) {
        Log.d(TAG, "resolveServiceWithDnsSd: regType $regType domain $domain")
        try {
            discoveryDisposable = rxDnsSd.browse(regType, domain ?: LOCAL_DOMAIN)
                .compose(rxDnsSd.resolve())
                .compose(rxDnsSd.queryIPRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bonjourService ->
                    if (bonjourService.isLost) {
                        Log.d(
                            TAG,
                            "resolveServiceWithDnsSd: service lost ${bonjourService.serviceName}"
                        )
                        onServiceLostCallback?.invoke(bonjourService.serviceName)
                    } else {
                        Log.d(
                            TAG,
                            "resolveServiceWithDnsSd: service found ${bonjourService.serviceName}"
                        )
                        val mdnsInfo = ServiceInfo.BonjourInfo(bonjourService)
                        onServiceFoundCallback?.invoke(MdnsInfo(mdnsInfo))
                    }
                }, { throwable -> Log.e(TAG, "resolveServiceWithDnsSd: failed", throwable) })
        } catch (e: Exception) {
            e.printStackTrace()
            onError?.invoke(e.message ?: "error resolving")
        } finally {
            processNextInQueue()
        }
    }

    private fun discoverWithNsdManager(serviceType: String) {
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    @SuppressLint("NewApi")
    fun resolveService(service: DiscoveredService) {
        Log.d(
            TAG,
            "resolveService: serviceType ${service::class.simpleName} resolving method $resolvingMethod"
        )
        when (service) {
            is DiscoveredService.BonjourInfo -> {
                when (resolvingMethod) {
                    ResolvingMethod.RxDnsSd -> {
                        resolveServiceWithDnsSd(service.getType(), service.getServiceDomain())
                    }

                    else -> return //bonjour info can't be resolved with NsdManager
                }
            }

            is DiscoveredService.NsdInfo -> {
                when (resolvingMethod) {
                    ResolvingMethod.NsdManager -> {
                        if (isServiceInfoCallbackMethodAvailable()) {
                            Log.d(TAG, "resolveService: using serviceInfoCallback for resolving")
                            nsdManager.registerServiceInfoCallback(
                                service.serviceInfo,
                                Executors.newSingleThreadExecutor(),
                                serviceInfoCallback!!
                            )
                        } else {
                            Log.d(TAG, "resolveService: using resolveService methodṣ for resolving")
                            nsdManager.resolveService(service.serviceInfo, resolverListener)
                        }
                    }

                    ResolvingMethod.RxDnsSd -> {
                        resolveServiceWithDnsSd(service.getType(), service.getServiceDomain())
                    }
                }
            }

            is DiscoveredService.ShortcutInfo ->
                resolveServiceWithDnsSd(service.getType(), service.getServiceDomain())
        }
    }

    fun startServiceDiscovery() {
        Log.d(TAG, "startServiceDiscovery: method $discoverMethod")
        try {
            multicastLock.acquire()
            when (discoverMethod) {
                DiscoverMethod.NsdManager -> discoverWithNsdManager(SERVICE_TYPE)
                DiscoverMethod.RxDnsSd -> discoverWithDnsSd()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NewApi") // has validation mechanism
    fun stopServiceDiscovery() {
        Log.i(TAG, "stopServiceDiscovery: disposing browser and unregistering listeners")
        if (this::discoveryDisposable.isInitialized) {
            discoveryDisposable.takeIf { !it.isDisposed }?.dispose()
        }
        if (this::resolverDisposable.isInitialized) {
            resolverDisposable.takeIf { !it.isDisposed }?.dispose()
        }
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
            if (isServiceInfoCallbackSupported) {
                Log.i(TAG, "stopServiceDiscovery: unregistering serviceInfoCallback")
                nsdManager.unregisterServiceInfoCallback(serviceInfoCallback!!)
            }

        } catch (e: IllegalArgumentException) {
            Log.i(TAG, "stopServiceDiscovery: found listener wasn't attached")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            multicastLock.release()
        }
    }

    private fun createDnsSd(context: Context): Rx2Dnssd {
        // https://developer.android.com/about/versions/12/behavior-changes-12#mdnsresponder
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i(TAG, "createDnsSd: Using embedded version of dns sd")
            Rx2DnssdEmbedded(context)
        } else {
            Log.i(TAG, "createDnsSd: Using bindable version of dns sd")
            Rx2DnssdBindable(context)
        }
    }

    /** check whether [NsdManager.registerServiceInfoCallback] is available on the device version*/
    private fun isServiceInfoCallbackMethodAvailable(): Boolean {
        // this method is available from T extension 7, this can be checked getExtensionVersion which needs at least R
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 7).also {
            Log.i(TAG, "isServiceInfoCallbackMethodAvailable: method available $it")
        }
    }

    companion object {
        private const val TAG = "ServiceDiscoveryManager"
        private const val SERVICE_TYPE = "_services._dns-sd._udp"
        const val LOCAL_DOMAIN = "local."
    }
}
