package eu.letmehelpu.android.jobexecutor.response

data class InitialPagedResponse<T>(
        override val list: List<T>
) : PagedResponse<T>()