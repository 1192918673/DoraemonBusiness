package com.geeknewbee.doraemon.http;

import com.geeknewbee.doraemon.control.Command;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SoundService {
    @FormUrlEncoded
    @POST("command/translate_sound")
    Call<BaseResponseBody<List<Command>>> translateSound(@Field("sound") String user);
}
