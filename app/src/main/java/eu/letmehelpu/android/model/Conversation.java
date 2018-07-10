package eu.letmehelpu.android.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class Conversation implements Serializable {

    public String documentId;

    public String lastMessage;

    public long lastMessageBy;

    public long offerId;

    public Long timestamp;

    public long transactionId;

    public int usersCount;

    public Map<String, Boolean> usersInactive;

    public Map<String, Boolean> users;

    public Conversation() {
    }

    public Conversation(ConversationDocument conversationDocument, String documentId) {
        this.documentId = documentId;
        this.lastMessage = conversationDocument.lastMessage;
        this.lastMessageBy = conversationDocument.lastMessageBy;
        this.offerId = conversationDocument.offerId;
        if(conversationDocument.timestamp != null) {
            this.timestamp = conversationDocument.timestamp.toDate().getTime();
        } else {
            this.timestamp = null;
        }
        this.transactionId = conversationDocument.transactionId;
        this.usersCount = conversationDocument.usersCount;
        this.usersInactive = conversationDocument.usersInactive;
        this.users = conversationDocument.users;
    }

    public ConversationDocument toConversationDocument() {
        ConversationDocument conversationDocument = new ConversationDocument();
        conversationDocument.lastMessage = this.lastMessage;
        conversationDocument.lastMessageBy = this.lastMessageBy;
        conversationDocument.offerId = this.offerId;
        if(this.timestamp != null) {
            conversationDocument.timestamp = new Timestamp(new Date(this.timestamp));
        } else {
            conversationDocument.timestamp = null;
        }
        conversationDocument.transactionId = this.transactionId;
        conversationDocument.usersCount = this.usersCount;
        conversationDocument.usersInactive = this.usersInactive;
        conversationDocument.users = this.users;
        return conversationDocument;
    }
}
