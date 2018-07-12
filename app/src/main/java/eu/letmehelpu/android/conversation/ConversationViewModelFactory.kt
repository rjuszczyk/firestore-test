package eu.letmehelpu.android.conversation

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.os.Handler
import eu.letmehelpu.android.conversationlist.paging.MovieListPagedDataProviderFactory
import eu.letmehelpu.android.jobexecutor.PageProviderExecutor
import eu.letmehelpu.android.model.Conversation
import eu.letmehelpu.android.model.Message
import java.util.concurrent.Executor

class ConversationViewModelFactory(
        val userId:Long,
        val conversation: Conversation,
        private val movieListPagedDataProviderFactory: MovieListPagedDataProviderFactory,
        private val pageProviderExecutor: PageProviderExecutor<Message>) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val handler = Handler()
        val e = Executor { p0 -> handler.post(p0) }
        @Suppress("UNCHECKED_CAST")
        return ConversationViewModel(userId, conversation,
                movieListPagedDataProviderFactory, pageProviderExecutor, e) as T
    }
}