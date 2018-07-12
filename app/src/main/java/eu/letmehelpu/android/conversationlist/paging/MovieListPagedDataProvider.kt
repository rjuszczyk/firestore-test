package eu.letmehelpu.android.conversationlist.paging

import com.google.firebase.Timestamp
import eu.letmehelpu.android.jobexecutor.PagedDataProvider
import eu.letmehelpu.android.jobexecutor.response.FollowingPagedResponse
import eu.letmehelpu.android.jobexecutor.response.InitialPagedResponse
import eu.letmehelpu.android.model.Message

class MovieListPagedDataProvider(
        private val moviesPageDataProvider: MoviesPageDataProvider,
        private val conversationId:String
) : PagedDataProvider<Message> {
    private val jobCalls = ArrayList<MoviesPageDataProvider.Cancelable>()
    override fun dispose() {
        for (jobCall in jobCalls) {
            jobCall.cancel()
        }
        jobCalls.clear()
    }

    override fun provideInitialData(
            onLoaded: (InitialPagedResponse<Message>) -> Unit,
            onFailed: (Throwable) -> Unit
    ) {
        val cancelable = moviesPageDataProvider.provideMoviePage(null, conversationId, true, object : MoviesPageDataProvider.Callback {
            override fun onSuccess(cancelable: MoviesPageDataProvider.Cancelable, moviesPage: MoviesPage) {
                jobCalls.remove(cancelable)
                onLoaded(InitialPagedResponse(moviesPage.messages))
            }

            override fun onFailed(cancelable: MoviesPageDataProvider.Cancelable, throwable: Throwable) {
                jobCalls.remove(cancelable)
                onFailed(throwable)
            }
        })

        jobCalls.add(cancelable)
    }

    override fun providePageData(
            lastImestamp: Timestamp,
            isAfter: Boolean,
            onLoaded: (FollowingPagedResponse<Message>) -> Unit,
            onFailed: (Throwable) -> Unit
    ) {
        val cancelable = moviesPageDataProvider.provideMoviePage(lastImestamp, conversationId, isAfter, object : MoviesPageDataProvider.Callback {
            override fun onSuccess(cancelable: MoviesPageDataProvider.Cancelable, moviesPage: MoviesPage) {
                jobCalls.remove(cancelable)
                val t =
                if(isAfter) {
                    moviesPage.messages.firstOrNull()?.timestamp
                } else {
                    moviesPage.messages.lastOrNull()?.timestamp
                }
                onLoaded(FollowingPagedResponse(t, moviesPage.messages))
            }

            override fun onFailed(cancelable: MoviesPageDataProvider.Cancelable, throwable: Throwable) {
                jobCalls.remove(cancelable)
                onFailed(throwable)
            }
        })

        jobCalls.add(cancelable)
    }
}
