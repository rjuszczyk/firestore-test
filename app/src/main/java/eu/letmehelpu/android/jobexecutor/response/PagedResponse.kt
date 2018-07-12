package eu.letmehelpu.android.jobexecutor.response

abstract class PagedResponse<T> {
    abstract val list: List<T>
}