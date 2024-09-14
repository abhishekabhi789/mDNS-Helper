package io.github.abhishekabhi789.mdnshelper

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import io.github.abhishekabhi789.mdnshelper.utils.ShortcutManager

@InstallIn(ActivityComponent::class)
@Module
object AppModule {
    @Provides
    fun getNsdHelper(@ActivityContext context: Context) = DnsSdHelper(context)

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    fun getShortcutManager(@ActivityContext context: Context): ShortcutManager? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager(context)
        } else {
            null
        }
    }
}
