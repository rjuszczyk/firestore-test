package eu.letmehelpu.android.jobexecutor

import com.google.firebase.Timestamp
import eu.letmehelpu.android.jobexecutor.response.PagedResponse
import eu.letmehelpu.android.jobexecutor.response.InitialPagedResponse
import eu.letmehelpu.android.jobexecutor.response.FollowingPagedResponse

abstract class Job<E : PagedResponse<T>, T> {
    abstract val callback: (E) -> Unit
    var state: State = State.NotStarted
}

class PageJob<T>(
        val page: Timestamp,
        override val callback: (FollowingPagedResponse<T>) -> Unit,
        val isAfter: Boolean = true
) : Job<FollowingPagedResponse<T>, T>()

class InitialJob<T>(
        override val callback: (InitialPagedResponse<T>) -> Unit
) : Job<InitialPagedResponse<T>, T>()