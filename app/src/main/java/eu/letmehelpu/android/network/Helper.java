package eu.letmehelpu.android.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Helper {
    public TokenService service;
    public Helper() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://us-central1-letmehelpu-140614.cloudfunctions.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(TokenService.class);
    }


}
