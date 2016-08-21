package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

import com.geeknewbee.doraemon.entity.event.InputTimeoutEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * 输入监听task
 * 当经历定义的时间后没有输入 则停止声音监听，需要唤醒
 */
public class InputTimeoutMonitorTask extends Thread {
    //超时时间
    public static final int OUT_TIME = 1000 * 30;
    private Context context;
    //上次输入时间
    private long lastInputTime;

    //是否正在运行
    private boolean isRunning = false;

    public InputTimeoutMonitorTask(Context context) {
        this.context = context;
        lastInputTime = new Date().getTime();
    }

    public synchronized void setInputFlag() {
        lastInputTime = new Date().getTime();
    }

    private synchronized long getLastTime() {
        return lastInputTime;
    }

    public void startMonitor() {
        if (isRunning)
            return;
        start();
    }

    @Override
    public void run() {
        super.run();
        isRunning = true;
        while (true) {
            Date now = new Date();
            if (now.getTime() - getLastTime() > OUT_TIME) {
                //当超过规定的时间要通知停止监听
                EventBus.getDefault().post(new InputTimeoutEvent());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
