package com.geeknewbee.doraemon.webservice;

import com.geeknewbee.doraemon.entity.AuthRobotResponse;
import com.geeknewbee.doraemon.entity.GetAnswerResponse;
import com.geeknewbee.doraemon.entity.GetMembersCountResponse;
import com.geeknewbee.doraemon.entity.ResponseAppVersion;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @GET("talking/answer")
    Call<BaseResponseBody<GetAnswerResponse>> getAnswer(@Query("token") String token, @Query("question") String question);

    @FormUrlEncoded
    @POST("auth/robot")
    Call<BaseResponseBody<AuthRobotResponse>> authRobot(@Field("serial_no") String serialNo, @Field("version") String version, @Field("sign") String sign);

    // 获取绑定的账户数
    @GET("robot/members_count")
    Call<BaseResponseBody<GetMembersCountResponse>> getMembersCount(@Query("token") String token);

    // 上传电量
    @FormUrlEncoded
    @PUT("robot/battery")
    Call<BaseResponseBody<Object>> robotBattery(@Field("token") String token, @Field("percent") int percent);

    // 上传SSID
    @FormUrlEncoded
    @PUT("robot/ssid")
    Call<BaseResponseBody<Object>> uploadSsid(@Field("token") String token, @Field("ssid") String ssid);

    // 上传版本名
    @Multipart
    @PUT("/robot/version")
    Call<BaseResponseBody<Object>> uploadVersionName(@Part("token") RequestBody token, @Field("ssid") RequestBody appVersionName);

    // 请求服务器版本号
    @GET("/robot/version")
    Call<BaseResponseBody<ResponseAppVersion>> checkVersionCode();

    // 上传照片
    @Multipart
    @POST("robot/photo")
    Call<BaseResponseBody<Object>> uploadPhoto(@Part("token") RequestBody token, @Part("photo_type") RequestBody photo_type, @Part("photo\"; filename=\"picture.jpg") RequestBody photo);
}