package me.nathanfallet.snapchatclone.android

import android.app.Application
import me.nathanfallet.snapchatclone.data.dataModule
import me.nathanfallet.snapchatclone.domain.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * The Android application, which assembles the layers with Koin.
 */
class SnapCloneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SnapCloneApplication)
            modules(domainModule, dataModule)
        }
    }
}
