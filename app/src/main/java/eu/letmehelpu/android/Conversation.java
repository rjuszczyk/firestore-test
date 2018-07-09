package eu.letmehelpu.android;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Conversation implements Serializable{

    String documentId;

    String lastMessage;

    long lastMessageBy;

    long offerId;

    long timestamp;

    long transactionId;

    int usersCount;

    Map<String,Boolean> usersInactive;

    Map<String,Boolean> users;

}
