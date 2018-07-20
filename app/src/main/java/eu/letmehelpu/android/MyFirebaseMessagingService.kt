package eu.letmehelpu.android

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import eu.letmehelpu.android.messaging.UserIdStoreage
import eu.letmehelpu.android.model.ConversationDocument
import eu.letmehelpu.android.model.Message
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {
    val gson = Gson()
    lateinit var userIdStoreage: UserIdStoreage
    override fun onCreate() {
        super.onCreate()
        userIdStoreage = UserIdStoreage(getSharedPreferences("messaging", Context.MODE_PRIVATE))
    }
    val TAG="RADEK"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM conversations here.
        // Not getting conversations here? See why this may be: https://goo.gl/39bRNJ


        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {

            val conversationId = remoteMessage.data["conversationId"]!!
            val messageFcm = gson.fromJson(remoteMessage.data["data"], MessageFcm::class.java)

            Log.d(TAG, "Message Notification Body: " + messageFcm.text)

            val db = FirebaseFirestore.getInstance()

            db.collection(AppConstant.COLLECTION_CONVERSATION).document(conversationId).get().addOnSuccessListener{
                var conversationDocument = it?.toObject(ConversationDocument::class.java)
                conversationDocument?.let{
                    if(it.timestamp.toDate().time >= messageFcm.getSendTimestamp().time) {

                        val lastReadTimestamp = conversationDocument.lastRead[userIdStoreage.userId.toString()] ?: Timestamp(Date(0))

                        loadMessagesAfterTimestamp(conversationId, lastReadTimestamp)
                    } else {
                        Log.d(TAG, "no in store")
                    }
                }
            }
        }


        // Check if message contains a notification payload.
        remoteMessage.notification?.let {



        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun loadMessagesAfterTimestamp(conversationId: String, time: Timestamp) {
        val db = FirebaseFirestore.getInstance()
        db.collection(AppConstant.COLLECTION_CONVERSATION)

                .document(conversationId)
                .collection("messages")
                .whereGreaterThanOrEqualTo("sendTimestamp", time)
                .orderBy("sendTimestamp", Query.Direction.DESCENDING)
                .get().addOnSuccessListener {
                    val messages = it.toObjects(Message::class.java)
                    val msgStr = StringBuilder()
                    for (message in messages) {
                        msgStr.append(message.text)
                        msgStr.append("\n")
                    }

                    Log.d(TAG, msgStr.toString())
                }

    }


    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

}