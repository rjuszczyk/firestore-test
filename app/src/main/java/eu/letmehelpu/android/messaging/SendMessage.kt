package eu.letmehelpu.android.messaging

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import eu.letmehelpu.android.AppConstant
import eu.letmehelpu.android.model.Message

class SendMessage {
    public fun sendMessage(conversationId:String , userId:Long, messageText: String) : Timestamp {
        //  if(1==1)return
        val message = Message()
        message.by = userId
        message.seen = false
        message.text = messageText
        message.timestamp = null//System.currentTimeMillis();
        message.sendTimestamp = Timestamp.now()//System.currentTimeMillis();
        val db = FirebaseFirestore.getInstance()

        db.collection(AppConstant.COLLECTION_CONVERSATION).document(conversationId)
                .collection("messages")
                .add(message)

        return message.sendTimestamp
    }
}