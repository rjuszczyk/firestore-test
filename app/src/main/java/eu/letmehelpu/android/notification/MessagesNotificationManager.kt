package eu.letmehelpu.android.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.RemoteInput
import android.support.v4.app.TaskStackBuilder
import eu.letmehelpu.android.MainActivity
import eu.letmehelpu.android.R
import eu.letmehelpu.android.conversation.ConversationActivity
import eu.letmehelpu.android.conversationlist.ConversationListActivity
import eu.letmehelpu.android.messaging.MessagingService
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.Message

class MessagesNotificationManager {
    fun createNotifciation(context: Context, conversation:Conversation, userId:Long, messages:List<Message>): Notification {
        var firstLine: String? = messages.firstOrNull()?.text
        val msgStr = StringBuilder()

        for (message in messages.asReversed()) {
            msgStr.append(message.text)
            msgStr.append("\n")
        }

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
                .setStyle(NotificationCompat.BigTextStyle().bigText(msgStr))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(replyAction)
                .addAction(dismissAction)
                .setContentIntent(resultPendingIntent)

        // notificationId is a unique int for each notification that you must define
        return mBuilder.build()
    }

    val KEY_REPLY = "message"

    private fun getReplyAction(context: Context, conversation:Conversation): NotificationCompat.Action {
        val replyLabel = "Enter your reply here"

        //Initialise RemoteInput
        var replyIntent = MessagingService.createSendMessageIntent(context, conversation)

        val remoteInput = RemoteInput.Builder(KEY_REPLY)
                .setLabel(replyLabel)
                .build()

        val replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, PendingIntent.FLAG_ONE_SHOT)

        val replyAction = NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat, "REPLY", replyPendingIntent)
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build()

        return replyAction
    }

    private fun getDismissAction(context: Context, conversation:Conversation) : NotificationCompat.Action {
        val intent = MessagingService.createDeleteNotificationIntent(context, conversation)
        val deletePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)


        val deleteAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel, "DISMISS", deletePendingIntent)
        return deleteAction.build()
    }
}