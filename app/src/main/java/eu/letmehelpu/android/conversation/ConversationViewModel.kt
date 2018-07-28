package eu.letmehelpu.android.conversation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PageKeyedDataSource
import android.arch.paging.PagedList
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import eu.letmehelpu.android.AppConstant
import eu.letmehelpu.android.conversationlist.paging.MovieListPagedDataProviderFactory
import eu.letmehelpu.android.messaging.SendMessage
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.ConversationDocument
import eu.letmehelpu.android.model.FirstMessage
import eu.letmehelpu.android.model.Message
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor

class ConversationViewModel(
        private val userId: Long,
        val conversation: Conversation,
        private val movieListPagedDataProviderFactory: MovieListPagedDataProviderFactory,
        mainThreadExecutor: Executor,
        private val sendMessage: SendMessage
) : ViewModel() {
    private val messages:LiveData<PagedList<Message>>
    private val userIdsToReadTimes = MutableLiveData<Map<Long, Long>>()

    private lateinit var registration2: ListenerRegistration

    init {
//        val x : android.arch.core.util.Function<Map<Long, Long>, PagedList<Message>>  = object : android.arch.core.util.Function<Map<Long, Long>, PagedList<Message>> {
//            override fun apply(input: Map<Long, Long>): PagedList<Message> {
//                return preparePagedList(mainThreadExecutor, conversation)
//            }
//
//        }
        userIdsToReadTimes.observeForever { latestDataSource?.invalidate() }
        messages = preparePagedList( mainThreadExecutor, conversation)
        //Transformations.map(userIdsToReadTimes, x)
        loadMessagesForConversation()
    }
    var lastRead = 0L
    var latestDataSource:DataSource<Timestamp, Message>? = null
    private fun preparePagedList(mainThreadExecutor: Executor, conversation: Conversation): LiveData<PagedList<Message>> {


        //val dataSource = pageKeyedDataSource(conversation)
        val dataSourceFactory = object : DataSource.Factory<Timestamp, Message>() {
            override fun create(): DataSource<Timestamp, Message> {
                Log.d("RADEK", "CREATE")
                val dataSource = pageKeyedDataSource(conversation)
                latestDataSource = dataSource
                return dataSource
            }
        }

        return LivePagedListBuilder(
                dataSourceFactory,
//                factory(),
                50)
                //.setFetchExecutor(IO_EXECUTOR)
                .build()
//        return PagedList.Builder(dataSource, 10)
//                .setNotifyExecutor(mainThreadExecutor)
//                .setFetchExecutor(mainThreadExecutor)
//                .
//                .build()
    }

    private fun pageKeyedDataSource(conversation: Conversation): DataSource<Timestamp, Message> {
        return object : PageKeyedDataSource<Timestamp, Message>() {
            override fun loadInitial(params: LoadInitialParams<Timestamp>, callback: LoadInitialCallback<Timestamp, Message>) {
                Log.d("LOADER", "loadInitial")
                val db = FirebaseFirestore.getInstance()
                var r: ListenerRegistration? = null
                val countDownLatch  = CountDownLatch(1)
                r = db.collection(AppConstant.COLLECTION_CONVERSATION)

                        .document(conversation.documentId)
                        .collection("messages")
                        .orderBy("sendTimestamp", Query.Direction.DESCENDING)
                        .limit(params.requestedLoadSize.toLong())
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                                p0?.let {
                                    val messages = it.toObjects(Message::class.java)


                                    val newest = messages.firstOrNull()?.sendTimestamp
                                    val oldest = messages.lastOrNull()?.sendTimestamp
                                    var result = ArrayList<Message>(messages.size+1)
                                    result.add(FirstMessage())
                                    result.addAll(messages)
                                    Log.d("LOADER", String.format("loadInitial -> %s %s", f(newest), f(oldest)))


                                    if(countDownLatch.count == 1L) {
                                        callback.onResult(result, newest, oldest)
                                        countDownLatch.countDown()
                                    } else {
                                        invalidate()
                                        r!!.remove()
                                    }
                                }
                            }
                        })

                countDownLatch.await()
            }

            override fun loadAfter(params: LoadParams<Timestamp>, callback: LoadCallback<Timestamp, Message>) {

                Log.d("LOADER", "loadAfter" + f(params.key))
                val countDownLatch  = CountDownLatch(1)

                val db = FirebaseFirestore.getInstance()
                var r: ListenerRegistration? = null
                r = db.collection(AppConstant.COLLECTION_CONVERSATION)
                        .document(conversation.documentId)
                        .collection("messages")
                        .orderBy("sendTimestamp", Query.Direction.DESCENDING)
                        .whereLessThan("sendTimestamp", params.key)
                        .limit(params.requestedLoadSize.toLong())

                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                                p0?.let {
                                    val messages = it.toObjects(Message::class.java)
                                    val newest = messages.firstOrNull()?.sendTimestamp
                                    val oldest = messages.lastOrNull()?.sendTimestamp

                                    Log.d("LOADER", String.format("loadAfter -> %s %s", f(newest), f(oldest)))

                                    callback.onResult(messages, oldest)
                                    r!!.remove()
                                    countDownLatch.countDown()
                                }
                            }
                        })

                countDownLatch.await()
            }

            override fun loadBefore(params: LoadParams<Timestamp>, callback: LoadCallback<Timestamp, Message>) {
                Log.d("LOADER", "loadBefore " + f(params.key))
                val db = FirebaseFirestore.getInstance()
                var r: ListenerRegistration? = null
                val countDownLatch  = CountDownLatch(1)
                r = db.collection(AppConstant.COLLECTION_CONVERSATION)
                        .document(conversation.documentId)
                        .collection("messages")
                        .orderBy("sendTimestamp", Query.Direction.ASCENDING)
                        .whereGreaterThan("sendTimestamp", params.key)
                        .limit(params.requestedLoadSize.toLong())

                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {
                                p0?.let {
                                    val messages = it.toObjects(Message::class.java)
                                    Collections.sort(messages, object : Comparator<Message> {
                                        override fun compare(p0: Message, p1: Message): Int {
                                            return p0.sendTimestamp.toDate().compareTo(p1.sendTimestamp.toDate())
                                        }
                                    })

                                    val newest = messages.firstOrNull()?.sendTimestamp
                                    val oldest = messages.lastOrNull()?.sendTimestamp

                                    Log.d("LOADER", String.format("loadBefore -> %s %s", f(newest), f(oldest)))

                                    callback.onResult(messages, newest)
                                    r!!.remove()
                                    countDownLatch.countDown()
                                }
                            }
                        })
                countDownLatch.await()
            }

            val f = SimpleDateFormat("HH:mm:ss")
            fun f(t: Timestamp?): String? {
                if (t == null) return null
                return f.format(t?.toDate())
            }
        }
    }


    private fun loadMessagesForConversation() {
        val db = FirebaseFirestore.getInstance()

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
                            Log.d("RADEK", "check if I can send")
                            if(otherUserReadsChanged(map)) {
                                userIdsToReadTimes.value = map
                                latestDataSource?.invalidate()
                            }

                            val readTime = it.timestamp.toDate().time
                            if(lastRead != readTime) {
                                lastRead = readTime
                                updateConversationWithLastRead(readTime)
                            }

                        }
                    }
                }
    }

    private fun otherUserReadsChanged(map: HashMap<Long, Long>) :Boolean {
        userIdsToReadTimes.value?.let {
            val oldMap = it
            for (entry in map.filter { it.key != userId }) {
                if(entry.value != oldMap[entry.key])
                    return true
            }
            return false
        }?: run { return true }
    }

    private fun updateConversationWithLastRead(lastMessageTime: Long) {
        conversation.lastRead[userId.toString()] = lastMessageTime
        updateLastRead(FirebaseFirestore.getInstance())
    }

    fun getMessages(): LiveData<PagedList<Message>> {
        return messages
    }

    fun getUserIdsToReadTimes(): LiveData<Map<Long, Long>> {
        return userIdsToReadTimes
    }

    fun sendMessage(messageText: String) {
        sendMessage.sendMessage(conversation.documentId, userId, messageText)
    }

    private fun updateLastRead(db: FirebaseFirestore) {
        val conversationDocument = conversation!!.toConversationDocument()

        Log.d("RADEK", "user " + userId + " is sending" + AppConstant.printReadTimes(conversation))

        db.collection(AppConstant.COLLECTION_CONVERSATION)
                .document(conversation!!.documentId)

                .update(FieldPath.of("lastRead", userId.toString()), conversationDocument.lastRead[""+userId]
                )
    }



    override fun onCleared() {
        registration2.remove()
    }
}