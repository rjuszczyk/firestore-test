package eu.letmehelpu.android.conversationlist

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class ConversationListViewModelFactory(private val userId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ConversationListViewModel(userId) as T
    }
}