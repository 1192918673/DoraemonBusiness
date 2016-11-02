package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.entity.GetMembersCountResponse;
import com.geeknewbee.doraemon.entity.event.NetWorkStateChangeEvent;
import com.geeknewbee.doraemon.entity.event.SetWifiCompleteEvent;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.BaseResponseBody;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 在每次设置wifi 后需要根据是有绑定的成员判断是否需要要显示二维码
 * 只有在机器猫还没有人绑定的时候会显示
 */
public class ShowQRTask extends Thread {
    private static final String TAG = ShowQRTask.class.getSimpleName();
    private static boolean TTS_TIPS_FLAG;
    private int number = 40;
    private String ssid;

    public ShowQRTask(String ssid) {
        this.ssid = ssid;
    }

    @Override
    public void run() {
        super.run();
        int index = 0;
        TTS_TIPS_FLAG = true;
        Context context = App.mContext;
        while (true) {
            if (index >= number) {
                break;
            }

            String token = DoraemonInfoManager.getInstance(context).getToken();
            if (DeviceUtil.isNetworkConnected(context) && !TextUtils.isEmpty(token)) {
                if (TTS_TIPS_FLAG) { // 解决配网成功等待扫码时，不断循环多次播报“网络已连接”
                    Doraemon.getInstance(context).addCommand(new SoundCommand("网络连接成功", SoundCommand.InputSource.TIPS));
                    EventBus.getDefault().post(new NetWorkStateChangeEvent(true));
                    TTS_TIPS_FLAG = false;
                }
                Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN, 3000);
                ApiService service = retrofit.create(ApiService.class);
                try {
                    Response<BaseResponseBody<GetMembersCountResponse>> response = service.getMembersCount(token).execute();
                    if (response.isSuccessful() && response.body().isSuccess()) {
                        if (response.body().getData().count == 0) {
                            EventBus.getDefault().post(new SetWifiCompleteEvent
                                    (true, false, ssid, "http://doraemon.microfastup.com/qr/" + DeviceUtil.getWifiMAC(context), DeviceUtil.getWIFILocalIpAdress(context)));//告知手机端连接成功
                            break;
                        } else {
                            EventBus.getDefault().post(new SetWifiCompleteEvent
                                    (true, true, ssid, "http://doraemon.microfastup.com/qr/" + DeviceUtil.getWifiMAC(context), DeviceUtil.getWIFILocalIpAdress(context)));//告知手机端连接失败
                            LogUtils.d(TAG, "二次绑定不用显示二维码，绑定用户数：" + response.body().getData().count);
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            index++;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
