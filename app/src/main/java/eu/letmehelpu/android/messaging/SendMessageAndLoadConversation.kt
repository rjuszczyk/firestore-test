package eu.letmehelpu.android.messaging

import android.util.Log
import com.google.firebase.Timestamp
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.Message
import io.reactivex.Single
import java.util.*

class SendMessageAndLoadConversation(
        val loadMessages: LoadMessages,
        val sendMessage: SendMessage) {
    fun sendAndLoad(conversation:Conversation, userId:Long, messageText: String) : Single<List<Message>> {
        val timestamp = sendMessage.sendMessage(conversation.documentId, userId, messageText)
        val lastRead = conversation.lastRead[userId.toString()]?: 0
        Log.d("RADEK_LAST_READ", "" + lastRead)
        return loadMessages.loadMessagesAfterTimestamp(conversation.documentId, Timestamp(Date(lastRead)), timestamp.toDate().time)
    }
}
