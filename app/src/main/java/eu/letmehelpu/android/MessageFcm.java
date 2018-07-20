package eu.letmehelpu.android;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MessageFcm {
    private static final SimpleDateFormat timestampFormat;
    static {
        timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        timestampFormat.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
    }
    String sendTimestamp;

    Date getSendTimestamp() {
       return null;
    }
}
