package eu.letmehelpu.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConversationsListActivity extends AppCompatActivity implements ConversationsAdapter.OnConversationSelectedListener {

    ConversationsAdapter conversationsAdapter = new ConversationsAdapter(this);
    private ListenerRegistration registration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long conversationId = 500;

        setContentView(R.layout.collection_list);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        registration = db.collection("conversations").whereEqualTo("transactionId", conversationId)
                //.orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        List<Conversation> conversations = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            if(!documentSnapshot.exists()) continue;
                            Conversation conversation = documentSnapshot.toObject(Conversation.class);
                            conversation.documentId = documentSnapshot.getId();
                            conversations.add(conversation);
                        }
                        Collections.sort(conversations, new Comparator<Conversation>() {
                            @Override
                            public int compare(Conversation o1, Conversation o2) {
                                return Float.compare(o1.timestamp, o2.timestamp);
                            }
                        });
                        conversationsAdapter.setConversations(conversations);
                    }
                });

        RecyclerView conversations = findViewById(R.id.conversations);
        conversations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        conversations.setAdapter(conversationsAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    @Override
    public void onCoversationSelected(Conversation conversation) {
        startActivity(ConversationActivity.createIntent(this, conversation));
    }
}
