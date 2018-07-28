package eu.letmehelpu.android

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import eu.letmehelpu.android.conversation.ConversationActivity
import eu.letmehelpu.android.messaging.MessagingService
import eu.letmehelpu.android.messaging.UserIdStoreage
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.ConversationDocument

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val gson = Gson()
    val TAG="RADEK"
    lateinit var userIdStoreage: UserIdStoreage
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting conversations here? See why this may be: https://goo.gl/39bRNJ

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {

            val conversationId = remoteMessage.data["conversationId"]!!
            val messageFcm = gson.fromJson(remoteMessage.data["data"], MessageFcm::class.java)

            Log.d(TAG, "Message Notification Body: " + messageFcm.text)

            if(conversationId != ConversationActivity.startedConversation) {
                loadConversation(conversationId, messageFcm.getSendTimestamp().time)
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {

        }
    }

    private fun loadConversation(conversationId: String, noOlderThan: Long) {
        val db = FirebaseFirestore.getInstance()

        db.collection(AppConstant.COLLECTION_CONVERSATION).document(conversationId).get().addOnSuccessListener {
            var conversationDocument = it?.toObject(ConversationDocument::class.java)
            conversationDocument?.let {
                if (it.timestamp.toDate().time >= noOlderThan) {
                    startServiceForegroundCompat(this@MyFirebaseMessagingService, MessagingService.createDisplayConversationIntent(this@MyFirebaseMessagingService, Conversation(conversationDocument, conversationId), noOlderThan))
                } else {
                    Log.d(TAG, "no in store")
                }
            }
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    companion object {
        fun startServiceForegroundCompat(context:Context, intent:Intent) {
            if(isMyServiceRunning(context, MessagingService.javaClass)) {
                context.startService(intent)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun isMyServiceRunning(context: Context, service:Class<Any>) : Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (runningService in manager.getRunningServices(Integer.MAX_VALUE)) {
                if(service.name.equals(service.name)) {
                    return true
                }
            }
            return false
        }
    }
}