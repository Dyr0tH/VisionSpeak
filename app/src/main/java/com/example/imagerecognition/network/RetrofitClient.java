package com.example.imagerecognition.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static ApiService getApiService(String baseUrl) {
        if (retrofit == null || !retrofit.baseUrl().toString().equals(baseUrl)) {
            // Add logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttpClient with increased timeout or no timeout
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    // Set connection timeout, read timeout, and write timeout to desired values
                    .connectTimeout(60, TimeUnit.SECONDS)  // Example: 60 seconds connection timeout
                    .writeTimeout(60, TimeUnit.SECONDS)    // Example: 60 seconds write timeout
                    .readTimeout(0, TimeUnit.SECONDS)     // Example: 60 seconds read timeout
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)  // Use custom OkHttpClient
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
