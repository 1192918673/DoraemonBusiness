package com.geeknewbee.doraemon.webservice;

import com.geeknewbee.doraemon.entity.AuthRobotResponse;
import com.geeknewbee.doraemon.processcenter.command.Command;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {
    @FormUrlEncoded
    @POST("command/translate_sound")
    Call<BaseResponseBody<List<Command>>> translateSound(@Field("sound") String user);

    @FormUrlEncoded
    @POST("auth/robot")
    Call<BaseResponseBody<AuthRobotResponse>> authRobot(@Field("serial_no") String serialNo, @Field("version") String version, @Field("sign") String sign);

    @FormUrlEncoded
    @PUT("robot/battery")
    Call<BaseResponseBody<Object>> robotBattery(@Field("token") String token, @Field("percent") int percent);
}
