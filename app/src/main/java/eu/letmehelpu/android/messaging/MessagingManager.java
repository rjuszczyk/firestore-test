package eu.letmehelpu.android.messaging;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import eu.letmehelpu.android.network.Helper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagingManager implements UserIdStoreage.UserIdChanged, MessagingTokenStoreage.MessagingTokenChanged {

    private final UserIdStoreage userIdStoreage;
    private final MessagingTokenStoreage messagingTokenStoreage;

    public MessagingManager(UserIdStoreage userIdStoreage, MessagingTokenStoreage messagingTokenStoreage) {
        this.userIdStoreage = userIdStoreage;
        this.messagingTokenStoreage = messagingTokenStoreage;
    }

    public void putUserId(@Nullable Long userId) {
        userIdStoreage.updateUserId(userId, this);
    }

    public void putMessagingToken(@Nullable String token) {
        messagingTokenStoreage.updateMessagingToken(token, this);
    }

    @Override
    public void onUserIdChanged(Long newUserId) {
        if(newUserId != null) {
            String token = messagingTokenStoreage.getMessagingToken();
            if(token != null) {
                sendUpdateToServer(newUserId, null, token);
            }
        }
    }

    @Override
    public void messagingTokenChanged(@Nullable String oldMessagingToken, String newMessagingToken) {
        if(newMessagingToken != null) {
            Long userId = userIdStoreage.getUserId();
            if(userId != null) {
                sendUpdateToServer(userId, oldMessagingToken, newMessagingToken);
            }
        }
    }

    private void sendUpdateToServer(@NonNull Long newUserId, @Nullable String oldToken, @NonNull String token) {
        new Helper().service.send(newUserId, token, oldToken).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("RADEK", "onResponse");
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("RADEK", "ERROR");
            }
        });
    }
}
