package eu.letmehelpu.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {

    MessagesAdapter adapter = new MessagesAdapter();
    long userId = 9;
    private Conversation document;
    private String TAG = this.getClass().getSimpleName();
    private ListenerRegistration registration;

    public static Intent createIntent(Context context, Conversation conversation) {
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra("conversationDocumentId", conversation);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        document = (Conversation) getIntent().getSerializableExtra("conversationDocumentId");

        setContentView(R.layout.conversation);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        registration = db.collection("conversations")
                .document(document.documentId)
                .collection("messages")
                //.orderBy("timestamp")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                List<Message> messages = queryDocumentSnapshots.toObjects(Message.class);
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return -Float.compare(o1.timestamp, o2.timestamp);
                    }
                });
                adapter.setMessages(messages);
            }
        });

        RecyclerView messages = findViewById(R.id.messages);
        messages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        messages.setAdapter(adapter);

        EditText messageInput = findViewById(R.id.message_input);
        messageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(v.getText().toString());
                    v.setText("");
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registration.remove();
    }

    public void sendMessage(final String text) {
        final Message message = new Message();
        message.seen = false;
        message.text = text;
        message.timestamp = System.currentTimeMillis();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("conversations").document(document.documentId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(this, new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        document.lastMessage = text;
                        document.lastMessageBy = userId;
                        document.timestamp =  message.timestamp;

                        db.collection("conversations")
                                .document(document.documentId)
                                .set(document)
                                .addOnCompleteListener(ConversationActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "onComplete() called with: task = [" + task + "]");
                                        Toast.makeText(ConversationActivity.this, "SENDED", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });


    }
}
