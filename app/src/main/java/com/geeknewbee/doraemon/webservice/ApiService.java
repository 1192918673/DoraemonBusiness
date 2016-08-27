package com.geeknewbee.doraemon.webservice;

import com.geeknewbee.doraemon.entity.AuthRobotResponse;
import com.geeknewbee.doraemon.entity.GetAnswerResponse;
import com.geeknewbee.doraemon.entity.GetMembersCountResponse;
import com.geeknewbee.doraemon.entity.ResponseAppVersion;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    @GET("talking/answer")
    Call<BaseResponseBody<GetAnswerResponse>> getAnswer(@Query("token") String token, @Query("question") String question);

    @FormUrlEncoded
    @POST("auth/robot")
    Call<BaseResponseBody<AuthRobotResponse>> authRobot(@Field("serial_no") String serialNo, @Field("version") String version, @Field("sign") String sign);

    @GET("robot/members_count")
    Call<BaseResponseBody<GetMembersCountResponse>> getMembersCount(@Query("token") String token);

    @FormUrlEncoded
    @PUT("robot/battery")
    Call<BaseResponseBody<Object>> robotBattery(@Field("token") String token, @Field("percent") int percent);

    @FormUrlEncoded
    @PUT("robot/ssid")
    Call<BaseResponseBody<Object>> uploadSsid(@Field("token") String token, @Field("ssid") String ssid);

    @FormUrlEncoded
    @PUT("/robot/version")
    Call<BaseResponseBody<Object>> uploadVersionName(@Field("token") String token, @Field("ssid") String appVersionName);

    @GET("/robot/version")
    Call<BaseResponseBody<ResponseAppVersion>> checkVersionCode();
}