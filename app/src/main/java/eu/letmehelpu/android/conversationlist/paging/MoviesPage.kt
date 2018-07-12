package eu.letmehelpu.android.conversationlist.paging

import com.google.firebase.Timestamp
import eu.letmehelpu.android.model.Message

data class MoviesPage(
        val messages:List<Message>
) {

//    fun nextPageKey() : Int? {
//        if(messages.isEmpty()) {
//            return null
//        } else {
//            return start + messages.size
//        }
//    }
}
