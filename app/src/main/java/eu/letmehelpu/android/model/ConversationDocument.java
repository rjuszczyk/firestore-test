package eu.letmehelpu.android.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Map;

public class ConversationDocument {

    public String lastMessage;

    public long lastMessageBy;

    public long offerId;

    @ServerTimestamp
    public Timestamp timestamp;

    public long transactionId;

    public int usersCount;

    public Map<String,Boolean> usersInactive;

    public Map<String,Boolean> users;

    public Map<String,Timestamp> lastRead;
}
