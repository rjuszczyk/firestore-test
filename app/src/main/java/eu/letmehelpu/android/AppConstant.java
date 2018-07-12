package eu.letmehelpu.android;

import com.google.firebase.Timestamp;

import eu.letmehelpu.android.model.Conversation;
import eu.letmehelpu.android.model.ConversationDocument;

public class AppConstant {
    public static final String COLLECTION_CONVERSATION = "conversations2";


    public static String printReadTimes(Conversation conversation) {
        Long read9 = conversation.lastRead.get("9");
        if(read9 == null) read9 = 0L;

        Long read10 = conversation.lastRead.get("10");
        if(read10 == null) read10 = 0L;

        return "\n 9->"+read9.toString()+"\n10->"+read10.toString()+"\n";
    }

    public static String printReadTimes(ConversationDocument conversation) {
        Timestamp read9 = conversation.lastRead.get("9");
        String read9s = "0";
        if(read9 != null) read9s = read9.toDate().getTime()+"";

        Timestamp read10 = conversation.lastRead.get("10");
        String read10s = "0";
        if(read10 != null) read10s = read9.toDate().getTime()+"";

        return "\n 9->"+read9s+"\n10->"+read10s+"\n";
    }
}
