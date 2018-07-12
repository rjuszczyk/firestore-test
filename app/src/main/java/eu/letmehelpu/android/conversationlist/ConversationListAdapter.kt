package eu.letmehelpu.android.conversationlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import eu.letmehelpu.android.model.Conversation

class ConversationListAdapter(private val userId:Long, private val conversationSelectedListener: OnConversationSelectedListener) : RecyclerView.Adapter<ConversationListAdapter.MessageViewHolder>() {

    internal var conversations: List<Conversation>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(TextView(parent.context))
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(conversations!![position])
    }

    override fun getItemCount(): Int {
        return conversations?.size ?: 0
    }

    fun setConversations(conversations: List<Conversation>) {
        this.conversations = conversations
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var conversation: Conversation? = null

        init {
            itemView.setOnClickListener {
                conversation?.let {
                    conversationSelectedListener.onConversationSelected(it)
                }
            }
        }

        fun bind(conversation: Conversation) {
            this.conversation = conversation
            if(hasNotReadMessages(conversation)) {
                (itemView as TextView).text = "last message=" + conversation.lastMessage + " (!)"
            } else {
                (itemView as TextView).text = "last message=" + conversation.lastMessage
            }
        }
    }

    fun hasNotReadMessages(conversation: Conversation) : Boolean {
        return conversation.timestamp?:0 > getLastMessageReadTimeByCurentUser(conversation)
    }

    fun getLastMessageReadTimeByCurentUser(conversation: Conversation) : Long {
        return conversation.lastRead[userId.toString()]?:0
    }

    interface OnConversationSelectedListener {
        fun onConversationSelected(conversation: Conversation)
    }
}
