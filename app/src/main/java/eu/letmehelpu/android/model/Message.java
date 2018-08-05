package eu.letmehelpu.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

import javax.annotation.Nullable;

public class Message implements Parcelable {

    public long by;

    public boolean seen;

    public String text;

    @ServerTimestamp
    @Nullable
    public Timestamp timestamp;

    public Timestamp sendTimestamp;

    public Message() {}

    protected Message(Parcel in) {
        by = in.readLong();
        seen = in.readByte() != 0;
        text = in.readString();
        long timestampLong = in.readLong();
        if(timestampLong == -1) {
            timestamp = null;
        } else {
            timestamp = new Timestamp(new Date(timestampLong));
        }
        sendTimestamp = new Timestamp(new Date(in.readLong()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(by);
        dest.writeByte((byte) (seen ? 1 : 0));
        dest.writeString(text);
        if(timestamp == null) {
            dest.writeLong(-1);
        } else {
            dest.writeLong(timestamp.toDate().getTime());
        }

        dest.writeLong(sendTimestamp.toDate().getTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

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