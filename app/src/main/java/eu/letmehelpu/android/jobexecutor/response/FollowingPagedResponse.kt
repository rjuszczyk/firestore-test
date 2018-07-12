package eu.letmehelpu.android.jobexecutor.response

import com.google.firebase.Timestamp

data class FollowingPagedResponse<T>(
        val page: Timestamp?,
        override val list: List<T>
) : PagedResponse<T>()