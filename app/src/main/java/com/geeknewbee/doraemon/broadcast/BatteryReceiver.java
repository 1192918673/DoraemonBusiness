package com.geeknewbee.doraemon.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.geeknewbee.doraemon.processcenter.DoraemonInfoManager;

/**
 * 电量监控
 */
public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);//获取当前电量
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);//电量的总刻度
            int batteryPerent = (level * 100) / scale;

            DoraemonInfoManager.getInstance(context).uploadBattery(batteryPerent);
        }
    }
}
