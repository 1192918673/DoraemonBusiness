package com.geeknewbee.doraemonsdk.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.geeknewbee.doraemonsdk.BaseApplication;

/**
 * 开启启动
 */
public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent arg1) {
        String packageName = BaseApplication.mContext.getPackageName();
        Intent launchIntent = BaseApplication.mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
    }
}
