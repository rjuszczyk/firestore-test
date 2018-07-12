package eu.letmehelpu.android.conversation

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import eu.letmehelpu.android.abs.AbsPagedListAdapter
import eu.letmehelpu.android.conversationlist.paging.MovieItemCallback
import eu.letmehelpu.android.model.Message

class MessagesListAdapter(
        retryListener: RetryListener,
        private val otherUsers:Array<Long>) : AbsPagedListAdapter<Message>(MovieItemCallback(), retryListener) {
    override fun createProgressViewHolder(parent: ViewGroup): ProgressViewHolder {
        return ProgressViewHolder(ProgressBar(parent.context))
    }

    override fun createFailedViewHolder(parent: ViewGroup, retryListener: RetryListener): FailedViewHolder<Message> {
        val tv = Button(parent.context)
        val height = (parent.context.resources.displayMetrics.density*50).toInt()
        tv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        return MyFailedViewHolder(tv, retryListener)
    }

    override fun createItemViewHolder(parent: ViewGroup): ItemViewHolder<Message> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    internal var userIdsToReadTimes:Map<Long, Long>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val tv = TextView(parent.context)
        var lp = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (parent.resources.displayMetrics.density* 70).toInt())

        tv.layoutParams = lp
        return MessageViewHolder(tv)
    }
//
//    fun setMessages(messages: List<Message>) {
//        this.messages = messages
//        notifyDataSetChanged()
//    }

    fun setLastReaded(userIdsToReadTimes:Map<Long, Long> ) {
        this.userIdsToReadTimes = userIdsToReadTimes
        notifyDataSetChanged()
    }

    fun getReadTimeForUserId(userId: Long): Long {
        userIdsToReadTimes?.let {
            it[userId]?.let {
               return it
            }?: run {
                return 0
            }
        } ?: run {
            return 0
        }
    }

    inner class MessageViewHolder(itemView: View) : ItemViewHolder<Message>(itemView){
        override fun bind(item: Message?) {
            item?.let {
                val message = it
                val send = message.timestamp != null
                itemView.alpha = if (send) 1f else 0.5f

                if(send) {
                    var readByAllUsers =  isMessageReadByAllusers(message)
                    if(readByAllUsers) {
                        (itemView as TextView).text = "[READ]" +  message.text
                    } else {
                        (itemView as TextView).text = "[NOT READ]" +  message.text
                    }
                } else {
                    (itemView as TextView).text = "[NOT READ]" +  message.text
                }
            }

        }
    }

    private fun isMessageReadByAllusers(message: Message) :Boolean {
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

    class MyFailedViewHolder(
            itemView: View,
            retryListener: RetryListener
    ) : FailedViewHolder<Message>(itemView) {

        init {
            itemView.setOnClickListener({retryListener.retryCalled()})
        }

        override fun bind(cause: Throwable) {
            (itemView as TextView).text = cause.localizedMessage
        }
    }
}
