package io.github.abhishekabhi789.mdnshelper

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.abhishekabhi789.mdnshelper.data.BrowserChoice
import io.github.abhishekabhi789.mdnshelper.utils.AppPreferences
import io.github.abhishekabhi789.mdnshelper.utils.CustomTabsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MdnsHelperApplication : Application() {
    @Inject
    lateinit var customTabsHelper: CustomTabsHelper

    @Inject
    lateinit var appPreferences: AppPreferences
    lateinit var preferredBrowserChoice: BrowserChoice
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            appPreferences.preferredBrowser.collectLatest {
                preferredBrowserChoice = it
            }
        }
    }
}
