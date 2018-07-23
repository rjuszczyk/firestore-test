package eu.letmehelpu.android.conversation

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import eu.letmehelpu.android.model.Conversation

class ConversationViewModelFactory(
        val userId:Long,
        val conversation: Conversation) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ConversationViewModel(userId, conversation) as T
    }
}