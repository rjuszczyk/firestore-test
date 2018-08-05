package eu.letmehelpu.android.di

import dagger.Module
import dagger.Provides
import eu.letmehelpu.android.di.scope.AppScope
import eu.letmehelpu.android.messaging.LoadMessages
import eu.letmehelpu.android.messaging.SendMessage

@Module
class MessagingModule {

    @Provides
    @AppScope
    fun provideSendMessage(): SendMessage {
        return SendMessage()
    }

    @Provides
    @AppScope
    fun provideLoadMessages(): LoadMessages {
        return LoadMessages()
    }
}