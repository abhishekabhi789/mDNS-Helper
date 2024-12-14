package io.github.abhishekabhi789.mdnshelper.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import io.github.abhishekabhi789.mdnshelper.nsd.ServiceDiscoveryManager
import io.github.abhishekabhi789.mdnshelper.utils.AppPreferences
import javax.inject.Singleton

@InstallIn(ActivityComponent::class)
@Module
object ServiceDiscoveryModule {
    @Provides
    @Singleton
    fun getServiceDiscoveryManager(
        @ActivityContext context: Context,
        appPreferences: AppPreferences
    ): ServiceDiscoveryManager {
        return ServiceDiscoveryManager(context,appPreferences)
    }
}
