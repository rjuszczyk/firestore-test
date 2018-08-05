package eu.letmehelpu.android

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import eu.letmehelpu.android.di.DaggerAppComponent

class LmhuApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
                .create(this)
    }
}