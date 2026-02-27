package com.seedtrac.lotgen.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seedtrac.lotgen.utils.AppConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    public static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(){
        if (retrofit == null){

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS) // default is 10s
                    .readTimeout(60, TimeUnit.SECONDS)    // default is 10s
                    .writeTimeout(60, TimeUnit.SECONDS)   // default is 10s
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConfig.COMMON_URL)
                    .client(okHttpClient)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
