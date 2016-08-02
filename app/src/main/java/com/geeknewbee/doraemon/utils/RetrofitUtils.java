package com.geeknewbee.doraemon.utils;


import com.geeknewbee.doraemon.center.command.CommandType;
import com.geeknewbee.doraemon.json.EnumSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtils {
    private static SSLSocketFactory sslSocketFactory;
    private static Map<String, Retrofit> retrofitMap = new HashMap<>();

    public static void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        RetrofitUtils.sslSocketFactory = sslSocketFactory;
    }

    public synchronized static Retrofit getRetrofit(String urlDomain) {
        if (retrofitMap.containsKey(urlDomain))
            return retrofitMap.get(urlDomain);
        else {
            Retrofit retrofit = createRetrofit(urlDomain);
            retrofitMap.put(urlDomain, retrofit);
            return retrofit;
        }
    }

    private static Retrofit createRetrofit(String urlDomain) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (urlDomain.startsWith("https") && sslSocketFactory != null)
            builder.sslSocketFactory(sslSocketFactory);
        OkHttpClient client = builder.build();

//        client.interceptors().add(new Interceptor() {
//            @Override
//            public Response intercept(Interceptor.Chain chain) throws IOException {
//                Request original = chain.request();
//                Request request = original.newBuilder()
//                        .method(original.method(), original.body())
//                        .build();
//
//                return chain.proceed(request);
//            }
//        });

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .registerTypeAdapter(CommandType.class, new EnumSerializer())
                .create();
        return new Retrofit.Builder()
                .baseUrl(urlDomain)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }
}
