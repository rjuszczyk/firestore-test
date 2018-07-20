package eu.letmehelpu.android.messaging;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class UserIdStoreage {
    private final SharedPreferences sharedPreferences;

    public UserIdStoreage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void updateUserId(@Nullable Long userId, UserIdChanged userIdChanged) {
        Long oldUserId = getUserId();
        if(oldUserId == null) {
            if(userId != null) {
                setUserId(userId);
                userIdChanged.onUserIdChanged(userId);
            }
            return;
        }

        if(!oldUserId.equals(userId)) {
            setUserId(userId);
            userIdChanged.onUserIdChanged(userId);
        }
    }

    public void setUserId(@Nullable Long userId) {
        if(userId == null) {
            sharedPreferences.edit().remove("userId").apply();
        } else {
            sharedPreferences.edit().putLong("userId", userId).apply();
        }
    }

    public Long getUserId() {
        long userId = sharedPreferences.getLong("userId", -1L);
        if(userId == -1) {
            return null;
        } else {
            return userId;
        }
    }

    interface UserIdChanged {
        void onUserIdChanged(Long newUserId);
    }
}
