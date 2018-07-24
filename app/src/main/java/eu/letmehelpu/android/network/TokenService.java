package eu.letmehelpu.android.network;

import android.support.annotation.Nullable;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface TokenService {
    @FormUrlEncoded
    @POST("registerToken")
    Call<String> send(@Field("userId") long userId, @Field("token") String token, @Nullable @Field("oldToken") String oldToken);
}
