package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.util.Log;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * 输入监听task
 * 当经历定义的时间后没有输入 则停止声音监听，等待唤醒
 */
public class InputTimeoutMonitorTask extends Thread {

    public static final String TAG = AISpeechEar.class.getSimpleName();
    //超时时间
    public static final int OUT_TIME = 1000 * 10;
    private Context context;
    //上次输入时间
    private long begintTime;

    //是否正在运行
    private boolean isRunning = false;
    //是否正在监听
    private boolean isMonitor;

    public InputTimeoutMonitorTask(Context context) {
        this.context = context;
        begintTime = new Date().getTime();
    }

    private synchronized long getBeginTime() {
        return begintTime;
    }

    /**
     * 开始计算ASR监听时间
     */
    public synchronized void startMonitor() {
        if (!isRunning)
            start();
        begintTime = new Date().getTime();
        isMonitor = true;
        LogUtils.d(TAG, "Start Timeout Monitor Listener...");
    }

    /**
     * 停止计算ASR监听时间
     */
    public synchronized void stopMonitor() {
        isMonitor = false;
        LogUtils.d(TAG, "Stop Timeout Monitor Listener...");
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
                    EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                    Doraemon.getInstance(App.mContext).addCommand(new SoundCommand("不说话，我去休息了", SoundCommand.InputSource.TIPS));
                    stopMonitor();
                }
            }
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }
}
