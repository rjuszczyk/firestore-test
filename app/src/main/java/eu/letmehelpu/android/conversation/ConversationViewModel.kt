package eu.letmehelpu.android.conversation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.Message

class ConversationViewModel(val userId:Long, val conversation: Conversation) : ViewModel() {
    private val messages = MutableLiveData<List<Message>>()
    private lateinit var registration: ListenerRegistration

    init {
        loadMessagesForConversation()
    }

    private fun loadMessagesForConversation() {
        val db = FirebaseFirestore.getInstance()
        registration = db.collection("conversations")
                .document(conversation!!.documentId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { queryDocumentSnapshots, e ->
                    queryDocumentSnapshots?.let {
                        val loadedMessages = it.toObjects(Message::class.java)
                        messages.value = loadedMessages
                    }
                }
    }

    fun getMessages(): LiveData<List<Message>> {
        return messages
    }

    fun sendMessage(messageText: String) {
        val message = Message()
        message.by = userId
        message.seen = false
        message.text = messageText
        message.timestamp = null//System.currentTimeMillis();
        val db = FirebaseFirestore.getInstance()

        db.collection("conversations").document(conversation!!.documentId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener {
                    conversation.lastMessage = messageText
                    conversation.lastMessageBy = userId
                    val conversationDocument = conversation!!.toConversationDocument()
                    conversationDocument.timestamp = null

                    db.collection("conversations")
                            .document(conversation!!.documentId)
                            .set(conversationDocument)

                }
    }

    override fun onCleared() {
        registration.remove()
    }
}