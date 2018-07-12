package eu.letmehelpu.android.conversationlist.paging

import com.google.firebase.Timestamp


interface MoviesPageDataProvider {
    fun provideMoviePage(lastImestamp: Timestamp?, conversationId: String, isAfter: Boolean, callback: Callback): Cancelable

    interface Cancelable {
        fun cancel()
    }

    interface Callback {
        fun onSuccess(cancelable: Cancelable, moviesPage: MoviesPage)
        fun onFailed(cancelable: Cancelable, throwable: Throwable)
    }
}
