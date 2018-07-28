package eu.letmehelpu.android.messaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.notification.MessagesNotificationManager
import io.reactivex.disposables.Disposable
import java.util.*

class MessagingService : Service() {

    private val loadMessages = LoadMessages()
    private val sendMessage = SendMessage()
    private lateinit var userIdStoreage:UserIdStoreage
    private val messagesNotificationManager = MessagesNotificationManager()
    private val conversationToRegistration = HashMap<String, Disposable>()
    private val conversationToNotification = HashMap<String, Notification>()

    override fun onBind(intent: Intent?): IBinder {
        throw RuntimeException()
    }

    companion object {
        val SEND_MESSAGE_ACTION = "SEND_MESSAGE_ACTION"
        val NEW_MESSAGE_ACTION = "NEW_MESSAGE_ACTION"
        val DELETE_NOTIFICATION_ACTION = "DELETE_NOTIFICATION_ACTION"

        fun createSendMessageIntent(context: Context, conversation: Conversation) : Intent{
            val intent = Intent(context, MessagingService::class.java)
            intent.action = SEND_MESSAGE_ACTION
            intent.putExtra("conversation", conversation)
            return intent
        }

        fun createDisplayConversationIntent(context: Context, conversation: Conversation, mustInclude:Long) : Intent{
            val intent = Intent(context, MessagingService::class.java)
            intent.action = NEW_MESSAGE_ACTION
            intent.putExtra("conversation", conversation)
            intent.putExtra("mustInclude", mustInclude)
            return intent
        }

        fun createDeleteNotificationIntent(context: Context, conversation: Conversation) : Intent {
            val intent = Intent(context, MessagingService::class.java)
            intent.action = DELETE_NOTIFICATION_ACTION
            intent.putExtra("conversation", conversation)
            return intent
        }
    }

    override fun onCreate() {
        super.onCreate()
        userIdStoreage = UserIdStoreage(getSharedPreferences("messaging", Context.MODE_PRIVATE))
        createNotificationChannel()

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "messages"
            val description = "messages from chat"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("messages", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {

            if(it.action == SEND_MESSAGE_ACTION) {
                val conversation = intent.getSerializableExtra("conversation") as Conversation

                val message = getReplyMessage(intent)

                sendMessage(conversation, message)
            }
            if(it.action == NEW_MESSAGE_ACTION) {
                val mustInclude = intent.getLongExtra("mustInclude", -1)
                if (mustInclude  == -1L) throw IllegalArgumentException("construct only with createIntent method")
                val conversation = intent.getSerializableExtra("conversation") as Conversation

                displayChatOrLoggoutUser(conversation, mustInclude)
            }
            if(it.action == DELETE_NOTIFICATION_ACTION) {
                val conversation = intent.getSerializableExtra("conversation") as Conversation
                hideChat(conversation)
            }
        }

        return Service.START_REDELIVER_INTENT
    }

    fun sendMessage(conversation:Conversation, message: String) {
        userIdStoreage.userId?.let {
            sendMessage.sendMessage(conversation.documentId, it, message)
        }?: run {
            onUserLoggedOut()
        }
    }

    fun displayChatOrLoggoutUser(conversation:Conversation, mustInclude:Long) {
        userIdStoreage.userId?.let {
            displayChat(conversation, it, mustInclude)
        }?: run {
            onUserLoggedOut()
        }
    }

    fun displayChat(conversation:Conversation, userId:Long, mustInclude:Long) {
        if(!isDisplayed(conversation)) {
            val disposable = loadMessages.loadMessagesWithTimestamp(conversation, mustInclude).subscribe {
                val notification = messagesNotificationManager.createNotifciation(this, conversation, userId, it)
                val notificationId = getNotificationIdForConversation(conversation.documentId)
                if(isForeground()) {
                    startForeground(notificationId, notification)
                } else {
                    val notificationManager = NotificationManagerCompat.from(this@MessagingService)
                    notificationManager.notify(notificationId , notification)
                }
                conversationToNotification.put(conversation.documentId, notification)
            }
            setDisplayed(conversation, disposable)
        }
    }

    fun onUserLoggedOut() {
        val notificationManager = NotificationManagerCompat.from(this@MessagingService)

        for (entry in conversationToRegistration.entries) {
            entry.value.dispose()
            var notificationId = getNotificationIdForConversation(entry.key)
            notificationManager.cancel(notificationId)
        }

        conversationToRegistration.clear()
        conversationToNotification.clear()
        stopForeground(true)
    }

    var boundedConversation: Conversation? = null
    private fun isForeground() : Boolean {
        return boundedConversation != null
    }

    private fun setForegroundConversation(conversation: Conversation) {
        boundedConversation = conversation
    }

    private fun getNotificationIdForConversation(conversationId: String): Int {
        return conversationId.hashCode()
    }

    private fun setDisplayed(conversation: Conversation, disposable: Disposable) {
        conversationToRegistration.put(conversation.documentId, disposable)
    }

    private fun isDisplayed(conversation: Conversation): Boolean {
        return conversationToRegistration.containsKey(conversation.documentId)
    }

    fun hideChat(conversation: Conversation) {
        val disposable = conversationToRegistration.remove(conversation.documentId)

        disposable?.let {
            it.dispose()
            if(conversation == boundedConversation) {
                if(!conversationToRegistration.isEmpty()) {
                    val nextNotification = conversationToRegistration.entries.first().key
                    val notificationId = getNotificationIdForConversation(nextNotification)
                    val notification = conversationToNotification.remove(nextNotification)
                    notification?.let {
                        startForeground(notificationId, it)
                    }
                } else {
                    stopSelf()
                }
            }
        }
    }

    private fun getReplyMessage(intent: Intent): String {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)


        val charSequence = remoteInput.getCharSequence(
                "message")
        return charSequence!!.toString()
    }
}