package com.geeknewbee.doraemon.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.geeknewbee.doraemon.entity.event.NetWorkStateChangeEvent;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.DoraemonInfoManager;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DeviceUtil.isNetworkConnected(context)) {
            LogUtils.d(TAG, "网络已连接"); // 测试：代码联网没有触发广播
//            Doraemon.getInstance(context).addCommand(new SoundCommand("网络已连接", SoundCommand.InputSource.TIPS));
//            EventBus.getDefault().post(new NetWorkStateChangeEvent(true));
            DoraemonInfoManager.getInstance(context).requestTokenFromServer();
        } else {
            LogUtils.d(TAG, "网络已断开");
            EventBus.getDefault().post(new NetWorkStateChangeEvent(false));
            Doraemon.getInstance(context).addCommand(new SoundCommand("网络已断开", SoundCommand.InputSource.TIPS));
        }
    }
}
