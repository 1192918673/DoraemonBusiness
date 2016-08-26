package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.entity.GetMembersCountResponse;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.BaseResponseBody;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 在每次设置wifi 后需要根据是有绑定的成员判断是否需要要显示二维码
 * 只有在机器猫还没有人绑定的时候会显示
 */
public class ShowQRTask extends Thread {
    private static final int INIT_STATUS = 0, SHOW_QR = 1, HIDE_QR = 2;
    private static final String TAG = ShowQRTask.class.getSimpleName();
    private int number = 40;
    private int status = INIT_STATUS;

    @Override
    public void run() {
        super.run();
        int index = 0;
        status = SHOW_QR;
        Context context = App.mContext;
        while (true) {
            if (index >= number) {
                if (status == HIDE_QR)
                    Doraemon.getInstance(context).addCommand(new Command(CommandType.BIND_ACCOUNT_SUCCESS));
                return;
            }

            String token = DoraemonInfoManager.getInstance(context).getToken();
            if (DeviceUtil.isNetworkConnected(context) && !TextUtils.isEmpty(token)) {
                Doraemon.getInstance(context).addCommand(new SoundCommand("网络已连接", SoundCommand.InputSource.TIPS));
                if (TextUtils.isEmpty(token)) return;
                Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
                ApiService service = retrofit.create(ApiService.class);
                try {
                    Response<BaseResponseBody<GetMembersCountResponse>> response = service.getMembersCount(token).execute();
                    if (response.isSuccessful() && response.body().isSuccess()) {
                        if (status == SHOW_QR) {
                            if (response.body().getData().count == 0) {
                                LogUtils.d(TAG, "显示二维码");
                                Doraemon.getInstance(context).addCommand(new Command(CommandType.SHOW_QR, "http://doraemon.microfastup.com/qr/" + DeviceUtil.getWifiMAC(context)));
                                status = HIDE_QR;
                                index = 0;
                            } else {
                                LogUtils.d(TAG, "二次绑定不用显示二维码");
                                return;
                            }
                        }
                        if (status == HIDE_QR) {
                            LogUtils.d(TAG, "应该隐藏了，还未隐藏" + response.body().getData().count);
                            if (response.body().getData().count > 0) {
                                LogUtils.d(TAG, "二维码已经隐藏" + response.body().getData().count);
                                Doraemon.getInstance(context).addCommand(new Command(CommandType.BIND_ACCOUNT_SUCCESS));
                                status = INIT_STATUS;
                                return;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            index++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
