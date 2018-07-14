package eu.letmehelpu.android.conversationlist.paging

import android.support.v7.util.DiffUtil
import android.util.Log
import eu.letmehelpu.android.model.FirstMessage
import eu.letmehelpu.android.model.Message

class MovieItemCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
//        throw RuntimeException()
        Log.d("CALLBACK", String.format("areItemsTheSame %s %s", oldItem, newItem))
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        if(oldItem is FirstMessage && newItem is FirstMessage) return true
        if(oldItem is FirstMessage || newItem is FirstMessage) return false
//        throw RuntimeException()
        Log.d("CALLBACK", String.format("areContentsTheSame %s %s", oldItem, newItem))
        val oldItemVisibility = oldItem.timestamp == null
        val newItemVisibility = newItem.timestamp == null
        return oldItemVisibility == newItemVisibility &&
                oldItem.text.equals(newItem.text)
                //&& oldItem.sendTimestamp.equals(newItem.sendTimestamp)
    }
}
