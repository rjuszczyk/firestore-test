package eu.letmehelpu.android.conversation

import android.arch.paging.PagedListAdapter
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import eu.letmehelpu.android.model.FirstMessage
import eu.letmehelpu.android.model.Message

class MessagesListAdapter(
        private val otherUsers: List<Long>) :
        PagedListAdapter<Message, MessagesListAdapter.MessageViewHolder>(MessagesCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (viewType == 0) {
            val tv = TextView(parent.context)
            var lp = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (parent.resources.displayMetrics.density * 70).toInt())

            tv.layoutParams = lp
            return MessageViewHolder(tv)
        } else {
            val v = View(parent.context)
            v.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
            return FirstMessageViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        if (holder !is FirstMessageViewHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) !is FirstMessage) 0 else 1
    }

    internal var userIdsToReadTimes: Map<Long, Long>? = null

    fun setLastReaded(userIdsToReadTimes: Map<Long, Long>) {
        this.userIdsToReadTimes = userIdsToReadTimes
        notifyDataSetChanged()
    }

    fun getReadTimeForUserId(userId: Long): Long {
        userIdsToReadTimes?.let {
            it[userId]?.let {
                return it
            } ?: run {
                return 0
            }
        } ?: run {
            return 0
        }
    }

    open inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Message?) {
            item?.let { message ->
                val send = message.timestamp != null

                if (send) {
                    if (isMessageReadByAllusers(message)) {
                        (itemView as TextView).text = ""+ message.by + "[READ]" + message.text
                    } else {
                        (itemView as TextView).text = ""+ message.by + "[NOT READ]" + message.text
                    }
                } else {
                    (itemView as TextView).text = ""+ message.by + "[NOT SEND]" + message.text
                }
            }

        }
    }

    inner class FirstMessageViewHolder(itemView: View) : MessageViewHolder(itemView)

    private fun isMessageReadByAllusers(message: Message): Boolean {
        var readByAllUsers = true
        for (otherUserId in otherUsers) {
            val readTime = getReadTimeForUserId(otherUserId)
            val read = readTime >= message.timestamp!!.toDate().time

            if (!read) {
                readByAllUsers = false
                break
            }
        }
        return readByAllUsers
    }
}
