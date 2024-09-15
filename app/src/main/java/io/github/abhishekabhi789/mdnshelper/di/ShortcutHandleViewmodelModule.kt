package io.github.abhishekabhi789.mdnshelper.di

import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import io.github.abhishekabhi789.mdnshelper.shortcut.ShortcutManager

@InstallIn(ActivityComponent::class)
@Module
object ShortcutHandleViewmodelModule {

    @Provides
    fun getShortcutManager(@ActivityContext context: Context): ShortcutManager? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager(context)
        } else null
    }
}
