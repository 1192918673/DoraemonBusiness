package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.text.TextUtils;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.AuthRobotResponse;
import com.geeknewbee.doraemon.utils.PrefUtils;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.RetrofitCallBack;
import com.geeknewbee.doraemon.webservice.RetrofitHelper;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.geeknewbee.doraemonsdk.utils.MD5Util;

import retrofit2.Retrofit;

/**
 * Doraemon 的 token，电量等信息获取和更新
 */
public class DoraemonInfoManager {
    private volatile static DoraemonInfoManager instance;
    private Context context;

    private DoraemonInfoManager(Context context) {
        this.context = context;
    }

    public static DoraemonInfoManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DoraemonInfoManager.class) {
                if (instance == null) {
                    instance = new DoraemonInfoManager(context);
                }
            }
        }
        return instance;
    }

    public String getToken() {
        return PrefUtils.getString(context, Constants.KEY_TOKEN, Constants.EMPTY_STRING);
    }

    /**
     * 从服务器获取token
     */
    public synchronized void requestTokenFromServer() {
        if (!TextUtils.isEmpty(PrefUtils.getString(context, Constants.KEY_TOKEN, Constants.EMPTY_STRING)))
            return;

        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        ApiService service = retrofit.create(ApiService.class);
        String mac = DeviceUtil.getWifiMAC(context);
        String versionName = DeviceUtil.getVersionName(context);
        String param = String.format("serial_no=%s&version=%s&api_secret=%s", mac, versionName, Constants.API_SECRET);
        String sign = MD5Util.encrypt(param).toLowerCase();

        RetrofitHelper.sendRequest(service.authRobot(mac, versionName, sign), new RetrofitCallBack<AuthRobotResponse>() {
            @Override
            public void onSuccess(AuthRobotResponse response) {
                LogUtils.d(Constants.HTTP_TAG, "get token :" + response.getToken());
                PrefUtils.saveString(context, Constants.KEY_TOKEN, response.getToken());
                PrefUtils.saveString(context, Constants.KEY_HX_USERNAME, response.getHx_user().getUsername());
                PrefUtils.saveString(context, Constants.KEY_HX_USERPWD, response.getHx_user().getPassword());
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(Constants.HTTP_TAG, "get token error:" + error);
            }
        });
    }

    /**
     * 上传电量
     *
     * @param battery
     */
    public void uploadBattery(int battery) {
        if (TextUtils.isEmpty(getToken())) return;
        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        ApiService service = retrofit.create(ApiService.class);

        RetrofitHelper.sendRequest(service.robotBattery(getToken(), battery), new RetrofitCallBack<Object>() {
            @Override
            public void onSuccess(Object response) {
                LogUtils.d(Constants.HTTP_TAG, "upload battery success");
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(Constants.HTTP_TAG, "upload battery error :" + error);
            }
        });
    }
}
