package eu.letmehelpu.android.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.*
import android.support.v4.graphics.drawable.IconCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import eu.letmehelpu.android.MainActivity
import eu.letmehelpu.android.R
import eu.letmehelpu.android.conversation.ConversationActivity
import eu.letmehelpu.android.conversationlist.ConversationListActivity
import eu.letmehelpu.android.messaging.SendMessageService
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.Message


class MessagesNotificationManager(val context: Context) {
    private val notificationManager: NotificationManagerCompat =  NotificationManagerCompat.from(context)

    fun displayConversationNotification(conversation:Conversation, userId:Long, messages:List<Message>) {
        val notification = createNotifciation(conversation, userId, messages)
        val notificationId = getNotificationIdForConversation(conversation)
        Log.d("RADEK", "displaying ["+conversation.lastMessage+"] to [" + conversation.documentId+"]")
        notificationManager.notify(notificationId, notification)
    }

    fun cancelConversationNotification(conversation: Conversation) {
        val notificationId = getNotificationIdForConversation(conversation)
        notificationManager.cancel(notificationId)
    }

    private fun createNotifciation(conversation:Conversation, userId:Long, messages:List<Message>): Notification {
        var firstLine: String? = messages.firstOrNull()?.text


        // Create an Intent for the activity you want to start
        val mainActivity = Intent(context, MainActivity::class.java)
        val conversationList = ConversationListActivity.createIntent(context, userId)
        val resultIntent = ConversationActivity.createIntent(context, userId, conversation)
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntentWithParentStack(mainActivity)
        stackBuilder.addNextIntentWithParentStack(conversationList)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        // Get the PendingIntent containing the entire back stack
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val replyAction: NotificationCompat.Action = getReplyAction(context, conversation)
        val dismissAction: NotificationCompat.Action = getDismissAction(context, conversation)


        val mBuilder = NotificationCompat.Builder(context, "messages")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Nowa wiadomości")
                .setContentText(firstLine!!)
                .setStyle(prepareMessages(messages))
//                .setStyle(prepareMessagesOld(messages))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(replyAction)
                .addAction(dismissAction)
                .setContentIntent(resultPendingIntent)

        // notificationId is a unique int for each notification that you must define
        return mBuilder.build()
    }

    private fun prepareMessagesOld(messages:List<Message>) : NotificationCompat.Style {


//        val longDescription = SpannableStringBuilder()
//        longDescription.append("First Part Not Bold ")
//        val start = longDescription.length
//        longDescription.append("BOLD")
//        longDescription.setSpan(ForegroundColorSpan(-0x33ab00), start, longDescription.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        longDescription.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, longDescription.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        longDescription.append(" rest not bold")

        val msgStr = SpannableStringBuilder()

        for (message in messages.asReversed()) {
            val start = msgStr.length
            msgStr.append(getUserNameForId(message.by))
            msgStr.append(": ")
            msgStr.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, msgStr.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            msgStr.append(message.text)
            msgStr.append("\n")
        }
        return NotificationCompat.BigTextStyle().bigText(msgStr)
    }

    private fun prepareMessages( messages:List<Message>): NotificationCompat.Style? {



        val user = Person.Builder().setIcon(IconCompat.createWithResource(context, R.drawable.icon_test)).setName("user name").build()
        val style = NotificationCompat.MessagingStyle(user)
        style.setConversationTitle("conversation title")


        messages.reversed().forEach({
            style.addMessage(it.text, it.sendTimestamp.toDate().time, user)
//            style.addMessage(it.text, it.sendTimestamp.toDate().time, getUserNameForId(it.by))
        })

        return style
    }

    private fun getUserNameForId(by: Long): CharSequence {
        return "user " + by
    }

    val KEY_REPLY = "message"

    private fun getReplyAction(context: Context, conversation:Conversation): NotificationCompat.Action {
        val replyLabel = "Enter your reply here"

        //Initialise RemoteInput
        var replyIntent =
                SendMessageService.createSendMessageIntent(context, conversation)
                //MessagingService.createSendMessageIntent(context, conversation)

        val remoteInput = RemoteInput.Builder(KEY_REPLY)
                .setLabel(replyLabel)
                .build()

        val replyPendingIntent = PendingIntent.getService(context, 0, replyIntent, PendingIntent.FLAG_ONE_SHOT)

        val replyAction = NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat, "REPLY", replyPendingIntent)
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build()

        return replyAction
    }

    private fun getDismissAction(context: Context, conversation:Conversation) : NotificationCompat.Action {
        val intent = SendMessageService.createDismissConversationIntent(context, conversation)
        val deletePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)


        val deleteAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel, "DISMISS", deletePendingIntent)
        return deleteAction.build()
    }

    private fun getNotificationIdForConversation(conversation: Conversation): Int {
        return conversation.documentId.hashCode()
    }
}