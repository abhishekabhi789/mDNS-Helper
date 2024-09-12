package io.github.abhishekabhi789.mdnshelper

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@InstallIn(ActivityComponent::class)
@Module
object AppModule{
    @Provides
    fun getNsdHelper(@ActivityContext context: Context) = DnsSdHelper(context)
}
