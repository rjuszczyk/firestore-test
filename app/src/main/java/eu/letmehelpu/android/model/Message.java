package eu.letmehelpu.android.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import javax.annotation.Nullable;

public class Message {

    public long by;

    public boolean seen;

    public String text;

    @ServerTimestamp
    @Nullable
    public Timestamp timestamp;

}
