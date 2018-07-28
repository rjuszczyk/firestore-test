package eu.letmehelpu.android.conversation

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import eu.letmehelpu.android.MyFirebaseMessagingService
import eu.letmehelpu.android.R
import eu.letmehelpu.android.abs.AbsPagedListAdapter
import eu.letmehelpu.android.conversationlist.paging.MovieListPagedDataProviderFactory
import eu.letmehelpu.android.conversationlist.paging.MoviesPageDataProvider
import eu.letmehelpu.android.conversationlist.paging.MoviesPageDataProviderImpl
import eu.letmehelpu.android.jobexecutor.PageProviderExecutor
import eu.letmehelpu.android.messaging.MessagingService
import eu.letmehelpu.android.messaging.SendMessage
import eu.letmehelpu.android.model.Conversation

class ConversationActivity : AppCompatActivity() {

    private lateinit var adapter : MessagesListAdapter
   // private lateinit var userId: Long
    private lateinit var conversation: Conversation
    private lateinit var conversationViewModel: ConversationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val provider: MoviesPageDataProvider = MoviesPageDataProviderImpl()
        val mo = MovieListPagedDataProviderFactory(provider)

        val userId = intent.getLongExtra(EXTRA_USER_ID, -1)
        conversation = intent.getSerializableExtra(EXTRA_CONVERSATION) as Conversation
        val otherUsers = conversation.users.keys.map { it -> it.toLong() }.filter { it != userId }
        adapter =

                MessagesListAdapter(object : AbsPagedListAdapter.RetryListener {
                    override fun retryCalled() {
                        //movieListViewModel.retry()
                    }
                },
                        otherUsers.toTypedArray()

                        )

        setContentView(R.layout.conversation)

        val messages = findViewById<RecyclerView>(R.id.messages)
        messages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        messages.adapter = adapter


        conversationViewModel = ViewModelProviders.of(this, ConversationViewModelFactory(userId, conversation, mo, PageProviderExecutor(), SendMessage())).get(ConversationViewModel::class.java)

        conversationViewModel.getMessages().observe(this, Observer {
            it?.let{ adapter.submitList(it)}
        })

        conversationViewModel.getUserIdsToReadTimes().observe(this, Observer {
            it?.let { adapter.setLastReaded(it) }
        })

        val messageInput = findViewById<EditText>(R.id.message_input)
        messageInput.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                conversationViewModel.sendMessage(v.text.toString())
                v.text = ""
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onStart() {
        super.onStart()
        startedConversation = conversation.documentId
        startService(MessagingService.createDeleteNotificationIntent(this, conversation))
    }

    override fun onStop() {
        super.onStop()
        startedConversation = null
    }

    companion object {
        var startedConversation:String? = null
        private val EXTRA_CONVERSATION = "EXTRA_CONVERSATION"
        private val EXTRA_USER_ID = "EXTRA_USER_ID"

        fun createIntent(context: Context, userId: Long, conversation: Conversation): Intent {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(EXTRA_CONVERSATION, conversation)
            intent.putExtra(EXTRA_USER_ID, userId)
            return intent
        }
    }
}
