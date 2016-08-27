package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.AuthRobotResponse;
import com.geeknewbee.doraemon.entity.ResponseAppVersion;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
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
    private String appVersionName = "";
    private int appVersionCode = 1;

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
                EventManager.sendHxInfoEvent(response.getHx_user());
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

    /**
     * 上传最新版本号
     */
    public void uploadVersionCode() {
        if (TextUtils.isEmpty(getToken())) return;
        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        ApiService service = retrofit.create(ApiService.class);

        PackageManager manager = App.mContext.getPackageManager();
        try {
            PackageInfo pkgInfo = manager.getPackageInfo(App.mContext.getPackageName(), 0);
            appVersionName = pkgInfo.versionName; // 版本名
            appVersionCode = pkgInfo.versionCode; // 版本号
            LogUtils.d(Constants.HTTP_TAG, "VersionName:" + appVersionName + "VersionCode:" + appVersionCode);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(Constants.HTTP_TAG, "Get Version Code Or Verson Name Exception...");
        }

        RetrofitHelper.sendRequest(service.uploadVersionName(getToken(), appVersionName), new RetrofitCallBack<Object>() {

            @Override
            public void onSuccess(Object response) {
                LogUtils.d(Constants.HTTP_TAG, "upload VERSION CODE success");
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(Constants.HTTP_TAG, "upload VERSION CODE error:" + error);
            }
        });

        RetrofitHelper.sendRequest(service.checkVersionCode(), new RetrofitCallBack<ResponseAppVersion>() {
            @Override
            public void onSuccess(ResponseAppVersion responseAppVersion) {
                if (appVersionCode < responseAppVersion.getLast_version_code()) {
                    // TODO 需要下载安装 最新App
                    Doraemon.getInstance(App.mContext).addCommand(new SoundCommand("当前诶皮皮不是最新版本，请更新", SoundCommand.InputSource.TIPS));
                }
                LogUtils.d(Constants.HTTP_TAG, "check update success");
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(Constants.HTTP_TAG, "check update success" + error);
            }
        });
    }

    /**
     * 上传 SSID
     *
     * @param ssid
     */
    public void uploadSsid(String ssid) {
        if (TextUtils.isEmpty(getToken())) return;
        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        ApiService service = retrofit.create(ApiService.class);

        RetrofitHelper.sendRequest(service.uploadSsid(getToken(), ssid), new RetrofitCallBack<Object>() {
            @Override
            public void onSuccess(Object response) {
                LogUtils.d(Constants.HTTP_TAG, "upload SSID success");
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(Constants.HTTP_TAG, "upload SSID error" + error);
            }
        });
    }
}
