package io.github.abhishekabhi789.mdnshelper.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.abhishekabhi789.mdnshelper.nsd.DiscoverMethod
import io.github.abhishekabhi789.mdnshelper.nsd.ResolvingMethod
import io.github.abhishekabhi789.mdnshelper.utils.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewmodel @Inject constructor(private val appPreferences: AppPreferences) :
    ViewModel() {

    val discoveryMethod = appPreferences.discoveryMethod
        .stateIn(viewModelScope, SharingStarted.Lazily, appPreferences.getDefaultDiscoveryMethod())

    val resolvingMethod = appPreferences.resolvingMethod
        .stateIn(viewModelScope, SharingStarted.Lazily, appPreferences.getDefaultResolvingMethod())

    val resolvingMethods = discoveryMethod.map { discoverMethod ->
        discoverMethod.getSupportedResolvingMethod()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        discoveryMethod.value.getSupportedResolvingMethod()
    )

    fun updateDiscoveryMethod(method: DiscoverMethod) {
        Log.i(TAG, "updateDiscoveryMethod: method $method")
        viewModelScope.launch { appPreferences.setDiscoverMethod(method) }
    }

    fun updateResolvingMethod(method: ResolvingMethod) {
        Log.i(TAG, "updateResolvingMethod: method $method")
        viewModelScope.launch { appPreferences.setResolvingMethod(method) }
    }

    companion object {
        private const val TAG = "SettingsViewmodel"
    }
}
