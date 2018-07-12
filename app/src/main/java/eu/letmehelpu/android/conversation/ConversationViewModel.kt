package eu.letmehelpu.android.conversation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.paging.DataSource
import android.arch.paging.PageKeyedDataSource
import android.arch.paging.PagedList
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import eu.letmehelpu.android.AppConstant
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.ConversationDocument
import eu.letmehelpu.android.model.Message
import java.util.*
import java.util.concurrent.Executor
import eu.letmehelpu.android.jobexecutor.PageProviderExecutor
import eu.letmehelpu.android.conversationlist.paging.MovieListPagedDataProviderFactory

class ConversationViewModel(
        private val userId: Long,
        val conversation: Conversation,
        private val movieListPagedDataProviderFactory: MovieListPagedDataProviderFactory,
        private val pageProviderExecutor: PageProviderExecutor<Message>,
        private val mainThreadExecutor: Executor
) : ViewModel() {
    private val messages:LiveData<PagedList<Message>>
    private val userIdsToReadTimes = MutableLiveData<Map<Long, Long>>()

    private lateinit var registration2: ListenerRegistration

    init {
        pageProviderExecutor.repositoryState = {
//            repositoryState.postValue(it)
        }
        val x : android.arch.core.util.Function<Map<Long, Long>, PagedList<Message>>  = object : android.arch.core.util.Function<Map<Long, Long>, PagedList<Message>> {
            override fun apply(input: Map<Long, Long>): PagedList<Message> {
                return preparePagedList(mainThreadExecutor, conversation)
            }

        }

        messages = Transformations.map(userIdsToReadTimes, x)
        loadMessagesForConversation()
    }

    private fun preparePagedList(mainThreadExecutor: Executor, conversation: Conversation): PagedList<Message> {

        val dataSource: DataSource<Timestamp, Message>

        pageProviderExecutor.attachDataProvider(movieListPagedDataProviderFactory.create(conversation.documentId))

        dataSource = object : PageKeyedDataSource<Timestamp, Message>() {
            override fun loadInitial(params: LoadInitialParams<Timestamp>, callback: LoadInitialCallback<Timestamp, Message>) {
                pageProviderExecutor.loadInitialPage {

                    val nextPage = it.list.lastOrNull()?.timestamp ?: null
                    callback.onResult(it.list, null, nextPage)



                    val messagesFromServer = it.list.filter { it.timestamp != null }.map { it.timestamp }.firstOrNull()
                    messagesFromServer?.let {
                        val lastReadTime:Long = conversation.lastRead[userId.toString()]?:0
                        Log.d("RADEK", "user " + userId + " has last msg " + messagesFromServer.toDate().time)
                        Log.d("LASTMSG", "lastReadTime = " + lastReadTime +"\nlastMsgTime = " + it.toDate().time+"\n")
                        if(it.toDate().time > lastReadTime) {
                            updateConversationWithLastRead(it.toDate().time)
                        }
                    }
                }
            }

            override fun loadAfter(params: LoadParams<Timestamp>, callback: LoadCallback<Timestamp, Message>) {
                pageProviderExecutor.loadPage(params.key, {
                    val nextPage = it.list.lastOrNull()?.timestamp ?: null
                    callback.onResult(it.list, nextPage)
                }, true)
            }

            override fun loadBefore(params: LoadParams<Timestamp>, callback: LoadCallback<Timestamp, Message>) {
                pageProviderExecutor.loadPage(params.key, {
                    val nextPage = it.list.firstOrNull()?.timestamp ?: null
                    callback.onResult(it.list, nextPage)
                }, false)
            }
        }

        return PagedList.Builder(dataSource, 10)
                .setNotifyExecutor(mainThreadExecutor)
                .setFetchExecutor(mainThreadExecutor)
                .build()
    }
    
    
    private fun loadMessagesForConversation() {
        val db = FirebaseFirestore.getInstance()
//        registration = db.collection(AppConstant.COLLECTION_CONVERSATION)
//                .document(conversation!!.documentId)
//                .collection("messages")
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .addSnapshotListener { queryDocumentSnapshots, e ->
//                    queryDocumentSnapshots?.let {
//                        val loadedMessages = it.toObjects(Message::class.java)
//                        messages.value = loadedMessages
//
//
//                    }
//                }

        registration2 = db.collection(AppConstant.COLLECTION_CONVERSATION)
                .document(conversation!!.documentId).addSnapshotListener { queryDocumentSnapshots, e ->
                    queryDocumentSnapshots?.let {
                        val conversation = it.toObject(ConversationDocument::class.java)
                        conversation?.let {
                            val map = HashMap<Long, Long>()
                            map.putAll(it.lastRead
                                    .map { it ->
                                        Pair(
                                            it.key.toLong(),
                                            if(it.value==null) 0L else it.value.toDate().time
                                        )
                                    }
                                    .filter { it.first != userId }
                            )
                            Log.d("RADEK", "user " + userId + " is receiving" + AppConstant.printReadTimes(conversation))
                            userIdsToReadTimes.value = map
                        }
                    }
                }
    }

    private fun updateConversationWithLastRead(lastMessageTime: Long) {

        conversation.lastRead[userId.toString()] = lastMessageTime
        synchronizeConversation(FirebaseFirestore.getInstance())
    }

    fun getMessages(): LiveData<PagedList<Message>> {
        return messages
    }

    fun getUserIdsToReadTimes(): LiveData<Map<Long, Long>> {
        return userIdsToReadTimes
    }

    fun sendMessage(messageText: String) {
        val message = Message()
        message.by = userId
        message.seen = false
        message.text = messageText
        message.timestamp = null//System.currentTimeMillis();
        val db = FirebaseFirestore.getInstance()

        db.collection(AppConstant.COLLECTION_CONVERSATION).document(conversation!!.documentId)
                .collection("messages")
                .add(message)
    }

    private fun synchronizeConversation(db: FirebaseFirestore) {
        val conversationDocument = conversation!!.toConversationDocument()

        Log.d("RADEK", "user " + userId + " is sending" + AppConstant.printReadTimes(conversation))

        db.collection(AppConstant.COLLECTION_CONVERSATION)
                .document(conversation!!.documentId)

                .update(FieldPath.of("lastRead", userId.toString()), conversationDocument.lastRead[""+userId]
                )
    }



    override fun onCleared() {
//        registration.remove()
        registration2.remove()
    }
}