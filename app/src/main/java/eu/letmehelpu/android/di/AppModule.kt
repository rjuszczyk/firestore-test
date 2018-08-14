package eu.letmehelpu.android.di

import android.app.Application
import android.content.Context
import dagger.Module
import javax.inject.Named
import dagger.Binds
import eu.letmehelpu.android.LmhuApplication


@Module
abstract class AppModule {

    @Binds
    @Named("AppContext")
    internal abstract fun provideAppContext(app: LmhuApplication): Context
}