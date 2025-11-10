package com.example.closethub.networks;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    //private static final String BASE_URL = "http://10.0.2.2:3000";
    // Development environments
    public static final String DEV_LOCAL = "http://192.168.1.100:3000/api";
    public static final String DEV_NGROK = "https://f543eee710de.ngrok-free.app";
    public static final String PRODUCTION = "https://api.your-app.com/api";

    // Chọn environment tại đây
    public static final String BASE_URL = DEV_NGROK;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}
