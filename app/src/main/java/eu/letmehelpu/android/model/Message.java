package eu.letmehelpu.android.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Message {

    public long by;

    public boolean seen;

    public String text;

    @ServerTimestamp
    public Timestamp timestamp;

}
