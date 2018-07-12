package eu.letmehelpu.android.conversationlist

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import eu.letmehelpu.android.AppConstant
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.ConversationDocument
import java.util.ArrayList

class ConversationListViewModel(userId:Long) : ViewModel() {
    private val conversations : MutableLiveData<List<Conversation>> = MutableLiveData()
    private lateinit var registration: ListenerRegistration

    init {
        loadForUserId(userId)
    }

    private fun loadForUserId(userId:Long) {
        val db = FirebaseFirestore.getInstance()
        registration = db.collection(AppConstant.COLLECTION_CONVERSATION)
                .whereEqualTo(FieldPath.of("users", userId.toString()), true)
                // .orderBy("timestamp")
                .addSnapshotListener(EventListener { queryDocumentSnapshots, e ->
                    if (queryDocumentSnapshots == null) return@EventListener
                    val conversations = ArrayList<Conversation>()
                    for (documentSnapshot in queryDocumentSnapshots.documents) {
                        if (!documentSnapshot.exists()) continue
                        val conversationDocument = documentSnapshot.toObject(ConversationDocument::class.java)

                        conversations.add(Conversation(conversationDocument!!, documentSnapshot.id))
                    }

                    this.conversations.value = conversations
                })
    }

    fun getConversations() : LiveData<List<Conversation>> {
        return conversations
    }

    override fun onCleared() {
        registration.remove()
    }
}