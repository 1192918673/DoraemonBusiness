package com.geeknewbee.doraemon.webservice;


import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.json.EnumSerializer;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        return getRetrofit(urlDomain, Constants.HTTP_TIME_OUT);
    }

    public synchronized static Retrofit getRetrofit(String urlDomain, int connectTimeOut) {
        if (retrofitMap.containsKey(urlDomain))
            return retrofitMap.get(urlDomain);
        else {
            Retrofit retrofit = createRetrofit(urlDomain, connectTimeOut);
            retrofitMap.put(urlDomain, retrofit);
            return retrofit;
        }
    }

    private static Retrofit createRetrofit(String urlDomain) {
        return createRetrofit(urlDomain, Constants.HTTP_TIME_OUT);
    }

    private static Retrofit createRetrofit(String urlDomain, int connectTimeOut) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(connectTimeOut, TimeUnit.MILLISECONDS);
        builder.writeTimeout(connectTimeOut, TimeUnit.MILLISECONDS);
        builder.readTimeout(connectTimeOut, TimeUnit.MILLISECONDS);
        if (urlDomain.startsWith("https") && sslSocketFactory != null)
            builder.sslSocketFactory(sslSocketFactory);
        builder.connectTimeout(connectTimeOut, TimeUnit.MILLISECONDS);
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
