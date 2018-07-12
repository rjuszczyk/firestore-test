package eu.letmehelpu.android.conversationlist.paging

import eu.letmehelpu.android.jobexecutor.PagedDataProvider
import eu.letmehelpu.android.model.Message


class MovieListPagedDataProviderFactory(
        private val moviesPageDataProvider: MoviesPageDataProvider
) {

    fun create(conversationId: String) : PagedDataProvider<Message> {
        return MovieListPagedDataProvider(moviesPageDataProvider, conversationId)
    }
}
