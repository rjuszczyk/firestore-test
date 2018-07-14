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

    public Timestamp sendTimestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (by != message.by) return false;
        if (seen != message.seen) return false;
        if (text != null ? !text.equals(message.text) : message.text != null) return false;
        if (timestamp != null ? !timestamp.equals(message.timestamp) : message.timestamp != null)
            return false;
        return sendTimestamp != null ? sendTimestamp.equals(message.sendTimestamp) : message.sendTimestamp == null;
    }
}