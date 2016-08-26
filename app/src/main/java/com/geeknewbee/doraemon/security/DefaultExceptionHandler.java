package com.geeknewbee.doraemon.security;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import com.geeknewbee.doraemon.entity.event.CrashEvent;
import com.geeknewbee.doraemon.view.MainActivity;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;

    public DefaultExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        EventBus.getDefault().post(new CrashEvent());
        LogUtils.d("DefaultExceptionHandler", "crash:" + ex.getMessage());
        Intent intent = new Intent(context, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 0);

        //Following code will restart your application after 2 seconds
        AlarmManager mgr = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 3000,
                pendingIntent);

        Process.killProcess(Process.myPid());
        System.exit(2);
        context.startActivity(intent);
    }
}
