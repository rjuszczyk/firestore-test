package eu.letmehelpu.android.messaging;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class MessagingTokenStoreage {
    private final SharedPreferences sharedPreferences;

    public MessagingTokenStoreage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }
    
    public void updateMessagingToken(@Nullable String messagingToken, MessagingTokenChanged messagingTokenChanged) {
        String oldMessagingToken = getMessagingToken();
        setMessagingToken(messagingToken);
        if(oldMessagingToken == null) {
            if(messagingToken != null) {
                messagingTokenChanged. messagingTokenChanged(null, messagingToken);
            }
            return;
        }

        if(!oldMessagingToken.equals(messagingToken)) {
            messagingTokenChanged. messagingTokenChanged(oldMessagingToken, messagingToken);
        }
    }

    private void setMessagingToken(@Nullable String messagingToken) {
        if(messagingToken == null) {
            sharedPreferences.edit().remove("messagingToken").apply();
        } else {
            sharedPreferences.edit().putString("messagingToken", messagingToken).apply();
        }
    }

    public String getMessagingToken() {
        return sharedPreferences.getString("messagingToken", null);
    }

    interface MessagingTokenChanged {
        void  messagingTokenChanged(@Nullable String oldMessagingToken, String newMessagingToken);
    }
}
