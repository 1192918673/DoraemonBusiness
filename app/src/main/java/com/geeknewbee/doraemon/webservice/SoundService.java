package com.geeknewbee.doraemon.webservice;

import com.geeknewbee.doraemon.entity.WeatherEntity;
import com.geeknewbee.doraemon.processcenter.command.Command;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SoundService {
    @FormUrlEncoded
    @POST("command/translate_sound")
    Call<BaseResponseBody<List<Command>>> translateSound(@Field("sound") String user);

    @GET("whapi/json/weather")
    Call<WeatherEntity> queryWeather(@Query("timestamp") String timestamp, @Query("cityId") String cityId, @Query("token") String token, @Query("key") String key);
}
