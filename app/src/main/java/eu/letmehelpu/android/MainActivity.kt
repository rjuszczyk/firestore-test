package eu.letmehelpu.android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.*
import eu.letmehelpu.android.R
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    val adapter = MessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = FirebaseFirestore.getInstance()
        db.collection("conversations")

                .whereEqualTo("transactionId", 500)
                .addSnapshotListener { p0, _ ->
                    for (document in p0!!.documents) {

                        Log.d("RADEK",document.data.toString())
                    }
                }

        Handler().postDelayed({
         //   modifyData()
        }, 5000)

        val recycler= findViewById<RecyclerView>(R.id.messages_recycler)
        recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recycler.adapter = adapter
    }

    private fun modifyData() {
        val db = FirebaseFirestore.getInstance()
        val data:Map<String, Any> = HashMap()

        val conversation= Conversation()

        conversation.lastMessageBy = 12
        conversation.lastMessage = "looo"
        conversation.offerId= 51
        conversation.timestamp= 1
        conversation.transactionId= 500
        conversation.usersCount = 2
        conversation.usersInactive = HashMap()
        conversation.usersInactive.put("12",false)
        conversation.usersInactive.put("9",false)
        conversation.users= HashMap()
        conversation.usersInactive.put("12",true)
        conversation.usersInactive.put("9",true)

        db.collection("conversations").document("mi5IB8lnLpgSIujVvKoR")
                .set(conversation, SetOptions.merge())
    }
}
