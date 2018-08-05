package eu.letmehelpu.android.messaging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import dagger.android.AndroidInjection
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.Message
import eu.letmehelpu.android.notification.MessagesNotificationManager
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MessagingService : Service() {

    @Inject lateinit var loadMessages: LoadMessages
    @Inject
    lateinit var sendMessage: SendMessage
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
            val intent = Intent(SEND_MESSAGE_ACTION)
            intent.putExtra("conversation", conversation)

            return intent
        }

        fun createDisplayConversationIntent(context: Context, conversation: Conversation, messages: ArrayList<Message>) : Intent{
            val intent = Intent(context, MessagingService::class.java)
            intent.action = NEW_MESSAGE_ACTION
            intent.putExtra("conversation", conversation)
            intent.putExtra("messages", messages)
            return intent
        }

        fun createDeleteNotificationIntent(context: Context, conversation: Conversation) : Intent {
            val intent = Intent(DELETE_NOTIFICATION_ACTION)
            intent.putExtra("conversation", conversation)
            return intent
        }
    }

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
        userIdStoreage = UserIdStoreage(getSharedPreferences("messaging", Context.MODE_PRIVATE))
        createNotificationChannel()

        val intentFilter = IntentFilter()
        intentFilter.addAction(DELETE_NOTIFICATION_ACTION)
        intentFilter.addAction(SEND_MESSAGE_ACTION)

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(p0: Context, intent: Intent) {
                if(intent.action == SEND_MESSAGE_ACTION) {
                    val conversation = intent.getSerializableExtra("conversation") as Conversation

                    val message = getReplyMessage(intent)

                    sendMessage(conversation, message)
                }
                if(intent.action == DELETE_NOTIFICATION_ACTION) {
                    val conversation = intent.getSerializableExtra("conversation") as Conversation
                    stopForegroundForConversation(conversation.documentId)
                }
            }

        }, intentFilter)
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
            if(it.action == NEW_MESSAGE_ACTION) {
                val messages = intent.getParcelableArrayListExtra<Message>("messages")
                val conversation = intent.getSerializableExtra("conversation") as Conversation

                displayChatOrLoggoutUser(conversation, messages)
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

    fun displayChatOrLoggoutUser(conversation:Conversation, messages:List<Message>) {
        userIdStoreage.userId?.let {
            displayChat(conversation, it, messages)
        }?: run {
            onUserLoggedOut()
        }
    }

    fun displayChat(conversation:Conversation, userId:Long, messages:List<Message>) {
        if(!isDisplayed(conversation)) {
            val notification = messagesNotificationManager.createNotifciation(this, conversation, userId, messages)
            val notificationId = getNotificationIdForConversation(conversation.documentId)
            startForegrodundForConversation(conversation.documentId, notificationId, notification)

            val mustInclude = messages.first().sendTimestamp.toDate().time
            val disposable = loadMessages.loadMessagesWithTimestamp(conversation.documentId, mustInclude).subscribe {
                val notification = messagesNotificationManager.createNotifciation(this, conversation, userId, it)
                val notificationId = getNotificationIdForConversation(conversation.documentId)

                val notificationManager = NotificationManagerCompat.from(this@MessagingService)
                notificationManager.notify(notificationId , notification)
            }

            conversationToRegistration.put(conversation.documentId, disposable)
        }
    }


    var currentForegroundChat: String? = null
    private fun startForegrodundForConversation(documentId: String, notificationId: Int, notification: Notification) {
        currentForegroundChat = documentId
        startForeground(notificationId, notification)
        conversationToNotification.put(documentId, notification)
    }

    private fun stopForegroundForConversation(documentId: String) {
        val notification = conversationToNotification.remove(documentId)
        val disposable = conversationToRegistration.remove(documentId)
        disposable?.dispose()
        if(documentId.equals(currentForegroundChat)) {
            currentForegroundChat = null
            val notificationManager = NotificationManagerCompat.from(this@MessagingService)
            val notificationId = getNotificationIdForConversation(documentId)
            notificationManager.cancel(notificationId)

            if(conversationToNotification.isEmpty()) {
                stopForeground(true)
            } else {
                val nextDocumentId = conversationToNotification.entries.first().key
                val nextNotificationId = getNotificationIdForConversation(nextDocumentId)
                val nextNotification = conversationToNotification.entries.first().value

                startForeground(nextNotificationId, nextNotification)
            }
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

        currentForegroundChat = null
    }

    private fun getNotificationIdForConversation(conversationId: String): Int {
        return conversationId.hashCode()
    }

    private fun isDisplayed(conversation: Conversation): Boolean {
        return conversationToRegistration.containsKey(conversation.documentId)
    }

    private fun getReplyMessage(intent: Intent): String {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)


        val charSequence = remoteInput.getCharSequence(
                "message")
        return charSequence!!.toString()
    }
}