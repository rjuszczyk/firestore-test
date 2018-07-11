package eu.letmehelpu.android.conversation

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import eu.letmehelpu.android.model.Message

class MessagesListAdapter : RecyclerView.Adapter<MessagesListAdapter.MessageViewHolder>() {

    internal var messages: List<Message>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(TextView(parent.context))
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.setMessage(messages!! [position])
    }

    override fun getItemCount() : Int {
        return messages?.size?:0
    }

    fun setMessages(messages: List<Message>) {
        this.messages = messages
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setMessage(message:  Message) {
            (itemView as TextView).text = message.text
            itemView.alpha = if (message.timestamp == null) 0.5f else 1f
        }
    }
}
