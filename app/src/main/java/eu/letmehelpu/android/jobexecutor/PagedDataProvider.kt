package eu.letmehelpu.android.jobexecutor

import com.google.firebase.Timestamp
import eu.letmehelpu.android.jobexecutor.response.InitialPagedResponse
import eu.letmehelpu.android.jobexecutor.response.FollowingPagedResponse

interface PagedDataProvider<T> {
    fun provideInitialData(onLoaded: (InitialPagedResponse<T>) -> Unit, onFailed: (Throwable) -> Unit)
    fun providePageData(lastImestamp: Timestamp, isAfter:Boolean, onLoaded: (FollowingPagedResponse<T>) -> Unit, onFailed: (Throwable) -> Unit)
    fun dispose()
}