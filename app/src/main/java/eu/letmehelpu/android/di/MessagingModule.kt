package eu.letmehelpu.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import eu.letmehelpu.android.di.scope.AppScope
import eu.letmehelpu.android.messaging.LoadMessages
import eu.letmehelpu.android.messaging.SendMessage
import eu.letmehelpu.android.messaging.SendMessageAndLoadConversation
import eu.letmehelpu.android.notification.MessagesNotificationManager
import javax.inject.Named

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

    @Provides
    @AppScope
    fun provideSendMessageAndLoadConversation(loadMessages: LoadMessages, sendMessage: SendMessage): SendMessageAndLoadConversation {
        return SendMessageAndLoadConversation(loadMessages, sendMessage)
    }

    @Provides
    @AppScope
    fun provideMessagesNotificationManager(@Named("AppContext") context:Context) : MessagesNotificationManager{
        return MessagesNotificationManager(context)
    }
}