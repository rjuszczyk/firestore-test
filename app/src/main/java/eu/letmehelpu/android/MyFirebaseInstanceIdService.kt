package eu.letmehelpu.android

import android.content.Context
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.iid.FirebaseInstanceId
import eu.letmehelpu.android.messaging.MessagingManager
import eu.letmehelpu.android.messaging.MessagingTokenStoreage
import eu.letmehelpu.android.messaging.UserIdStoreage


class MyFirebaseInstanceIdService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d("Radek", "Refreshed token: " + refreshedToken!!)

        val manager = MessagingManager(UserIdStoreage(getSharedPreferences("messaging", Context.MODE_PRIVATE)), MessagingTokenStoreage(getSharedPreferences("messaging", Context.MODE_PRIVATE)))
        manager.putMessagingToken(refreshedToken)
        // If you want to send conversations to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(refreshedToken)
    }
}