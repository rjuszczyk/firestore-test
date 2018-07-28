package eu.letmehelpu.android.conversationlist

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import eu.letmehelpu.android.R
import eu.letmehelpu.android.conversation.ConversationActivity
import eu.letmehelpu.android.model.Conversation


class ConversationListActivity : AppCompatActivity(), ConversationListAdapter.OnConversationSelectedListener {

    private lateinit var conversationListAdapter:ConversationListAdapter
    private var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = intent.getLongExtra(EXTRA_USER_ID, -1)
        if(userId == -1L) throw RuntimeException()
        conversationListAdapter = ConversationListAdapter(userId, this)

        setContentView(R.layout.collection_list)
        val testImage = findViewById < ImageView >(R.id.testImage)
//            GlideApp.with(this).load("http://goo.gl/gEgYUd").into(testImage)

        val conversations = findViewById < RecyclerView >(R.id.conversations)
        conversations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        conversations.adapter = conversationListAdapter

        val conversationViewModel = ViewModelProviders.of(this, ConversationListViewModelFactory(userId)).get(ConversationListViewModel::class.java)
        conversationViewModel.getConversations().observe(this, Observer {
            it?.let { conversationListAdapter.setConversations(it) }
        })
    }

    override fun onConversationSelected(conversation: Conversation) {
        startActivity(ConversationActivity.createIntent(this, userId, conversation))
    }



    companion object {
        val EXTRA_USER_ID = "userId"
        fun createIntent(context: Context, userId: Long): Intent {
            val intent = Intent(context, ConversationListActivity::class.java)
            intent.putExtra(EXTRA_USER_ID, userId)
            return intent
        }
    }
}
