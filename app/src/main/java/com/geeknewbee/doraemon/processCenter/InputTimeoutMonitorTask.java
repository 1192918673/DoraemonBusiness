package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * 输入监听task
 * 当经历定义的时间后没有输入 则停止声音监听，需要唤醒
 */
public class InputTimeoutMonitorTask extends Thread {
    //超时时间
    public static final int OUT_TIME = 1000 * 10;
    private Context context;
    //上次输入时间
    private long begintTime;

    //是否正在运行
    private boolean isRunning = false;
    private boolean isMonitor;

    public InputTimeoutMonitorTask(Context context) {
        this.context = context;
        begintTime = new Date().getTime();
    }

    private synchronized long getBeginTime() {
        return begintTime;
    }

    public synchronized void startMonitor() {
        if (!isRunning)
            start();
        begintTime = new Date().getTime();
        isMonitor = true;
    }

    public synchronized void stopMonitor() {
        isMonitor = false;
    }

    @Override
    public void run() {
        super.run();
        isRunning = true;
        while (true) {
            if (isMonitor) {
                Date now = new Date();
                if (now.getTime() - getBeginTime() > OUT_TIME) {
                    //当超过规定的时间要通知停止监听
                    isMonitor = false;
                    EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
