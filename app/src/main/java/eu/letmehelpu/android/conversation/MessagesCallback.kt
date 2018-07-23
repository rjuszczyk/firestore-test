package eu.letmehelpu.android.conversation

import android.support.v7.util.DiffUtil
import eu.letmehelpu.android.model.FirstMessage
import eu.letmehelpu.android.model.Message

class MessagesCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        if(oldItem is FirstMessage && newItem is FirstMessage) return true
        if(oldItem is FirstMessage || newItem is FirstMessage) return false

        val oldItemVisibility = oldItem.timestamp == null
        val newItemVisibility = newItem.timestamp == null
        return oldItemVisibility == newItemVisibility &&
                oldItem.text.equals(newItem.text)
    }
}
