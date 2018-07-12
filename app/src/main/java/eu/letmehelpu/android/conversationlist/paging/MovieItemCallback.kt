package eu.letmehelpu.android.conversationlist.paging

import android.support.v7.util.DiffUtil
import eu.letmehelpu.android.model.Message

class MovieItemCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message?, newItem: Message?): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message?, newItem: Message?): Boolean {
        return oldItem == newItem
    }
}
