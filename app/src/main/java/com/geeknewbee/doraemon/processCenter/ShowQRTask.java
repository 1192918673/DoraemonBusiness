package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.entity.GetMembersCountResponse;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.BaseResponseBody;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 在每次设置wifi 后需要根据是有绑定的成员判断是否需要要显示二维码
 * 只有在机器猫还没有人绑定的时候会显示
 */
public class ShowQRTask extends Thread {
    private int number = 40;

    @Override
    public void run() {
        super.run();
        int index = 0;
        Context context = App.mContext;
        while (true) {
            if (index >= number)
                return;

            if (DeviceUtil.isNetworkConnected(context)) {
                Doraemon.getInstance(context).addCommand(new Command(CommandType.PLAY_SOUND, "网络已连接"));
                String token = DoraemonInfoManager.getInstance(context).getToken();
                if (TextUtils.isEmpty(token)) return;
                Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
                ApiService service = retrofit.create(ApiService.class);
                try {
                    Response<BaseResponseBody<GetMembersCountResponse>> response = service.getMembersCount(token).execute();
                    if (response.isSuccessful() && response.body().isSuccess() && response.body().getData().count == 0)
                        Doraemon.getInstance(context).addCommand(new Command(CommandType.SHOW_QR));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
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
