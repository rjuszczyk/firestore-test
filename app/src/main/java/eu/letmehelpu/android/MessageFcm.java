package eu.letmehelpu.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MessageFcm {
    //{"conversationId":"uo4iOgw1XU03kNAQRjbl","data":{"by":9,"seen":false,"sendTimestamp":"2018-07-20T17:28:16.469Z","text":"okok","timestamp":"2018-07-20T17:28:18.841Z"}}

    private static final SimpleDateFormat timestampFormat;
    static {
        timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        timestampFormat.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
    }

    long by;
    String sendTimestamp;
    String text;

    Date getSendTimestamp() {
        try {
            return timestampFormat.parse(sendTimestamp);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}