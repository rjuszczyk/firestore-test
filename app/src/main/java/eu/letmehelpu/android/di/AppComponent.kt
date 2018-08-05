package eu.letmehelpu.android.di


import eu.letmehelpu.android.di.scope.AppScope
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import eu.letmehelpu.android.LmhuApplication

@Component(modules = [
    MessagingModule::class,
    ActivityBuilder::class,
    AndroidSupportInjectionModule::class
])

@AppScope
interface AppComponent : AndroidInjector<LmhuApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<LmhuApplication>()
}
