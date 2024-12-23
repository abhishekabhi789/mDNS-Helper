package io.github.abhishekabhi789.mdnshelper.utils

import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice
import io.github.abhishekabhi789.mdnshelper.nsd.DiscoverMethod
import io.github.abhishekabhi789.mdnshelper.nsd.ResolvingMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {

    val discoveryMethod: Flow<DiscoverMethod> = dataStore.data.map { preferences ->
        preferences[DISCOVERY_METHOD_KEY]?.let { DiscoverMethod.valueOf(it) }
            ?: getDefaultDiscoveryMethod()
    }
    val resolvingMethod: Flow<ResolvingMethod> = dataStore.data.map { preferences ->
        preferences[RESOLVING_METHOD_KEY]?.let { ResolvingMethod.valueOf(it) }
            ?: getDefaultResolvingMethod()
    }

    val preferredBrowser: Flow<BrowserChoice> = dataStore.data.map { preference ->
        preference[PREFERRED_BROWSER]?.let { packageName ->
            when (packageName) {
                BrowserChoice.Default.packageName -> BrowserChoice.Default
                BrowserChoice.CustomTab.packageName -> BrowserChoice.CustomTab
                BrowserChoice.AskUser.packageName -> BrowserChoice.AskUser
                else -> BrowserChoice.SavedBrowser(packageName)
            }
        } ?: BrowserChoice.CustomTab
    }

    suspend fun setDiscoverMethod(method: DiscoverMethod) {
        dataStore.edit { preferences -> preferences[DISCOVERY_METHOD_KEY] = method.name }
        // RxDnsSd returns BonjourService, which cannot be resolved with NsdManager
        if (method == DiscoverMethod.RxDnsSd) setResolvingMethod(ResolvingMethod.RxDnsSd)
        Log.i(TAG, "setDiscoverMethod: preferred method set as $method")
    }

    suspend fun setResolvingMethod(method: ResolvingMethod) {
        dataStore.edit { preferences -> preferences[RESOLVING_METHOD_KEY] = method.name }
        Log.i(TAG, "setDiscoverMethod: preferred method set as $method")
    }

    suspend fun setPreferredBrowser(choice: BrowserChoice) {
        dataStore.edit { preference -> preference[PREFERRED_BROWSER] = choice.packageName }
        Log.i(TAG, "setPreferredBrowser: preferred browser set as $choice")
    }

    fun getDefaultDiscoveryMethod(): DiscoverMethod {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            DiscoverMethod.RxDnsSd else DiscoverMethod.NsdManager
    }

    fun getDefaultResolvingMethod(): ResolvingMethod {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            ResolvingMethod.RxDnsSd else ResolvingMethod.NsdManager
    }

    companion object {
        private const val TAG = "AppPreference"
        const val PREF_NAME = "app_preference"
        private val DISCOVERY_METHOD_KEY = stringPreferencesKey("discovery_method")
        private val RESOLVING_METHOD_KEY = stringPreferencesKey("resolving_method")
        private val PREFERRED_BROWSER = stringPreferencesKey("preferred_browser")
    }
}
