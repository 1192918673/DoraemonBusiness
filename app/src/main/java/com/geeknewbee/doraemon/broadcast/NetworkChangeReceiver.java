package com.geeknewbee.doraemon.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.geeknewbee.doraemon.processcenter.DoraemonInfoManager;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (DeviceUtil.isNetworkConnected(context))
            DoraemonInfoManager.getInstance(context).requestTokenFromServer();
    }
}
