package eu.letmehelpu.android.conversationlist.paging

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import eu.letmehelpu.android.AppConstant
import eu.letmehelpu.android.model.Message

class MoviesPageDataProviderImpl : MoviesPageDataProvider {
    override fun provideMoviePage(lastImestamp: Timestamp?, conversationId: String, isAfter: Boolean, callback: MoviesPageDataProvider.Callback): MoviesPageDataProvider.Cancelable {

        return object : MoviesPageDataProvider.Cancelable {
            val registration:ListenerRegistration
            init {
                val db = FirebaseFirestore.getInstance()


                registration = db.collection(AppConstant.COLLECTION_CONVERSATION)
                        .document(conversationId)
                        .collection("messages")
                        .orderByAfter(isAfter)
                        .afterOrBefore(isAfter, lastImestamp)
                        .limit(10)
                        .addSnapshotListener { queryDocumentSnapshots, e ->
                            queryDocumentSnapshots?.let {
                                val loadedMessages = it.toObjects(Message::class.java)


                                callback.onSuccess(this, MoviesPage(loadedMessages ))
                                cancel()
                            }
                        }
            }

            override fun cancel() {
                registration.remove()
            }

        }
    }
// null null 6 5 4 3 2 1
    fun Query.afterOrBefore(isAfter:Boolean, timestamp: Timestamp?) : Query {
        if(!isAfter && timestamp == null) throw RuntimeException()

        if(isAfter) {
            timestamp?.let {
                return this.whereLessThan("timestamp", it)
            } ?: run {
                return this
            }
        } else {
            return this.whereGreaterThan("timestamp", timestamp!!)
        }
    }

    fun Query.orderByAfter(isAfter:Boolean) : Query {
        if(isAfter) {
            return this.orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            return this.orderBy("timestamp", Query.Direction.ASCENDING)
        }
    }


}
