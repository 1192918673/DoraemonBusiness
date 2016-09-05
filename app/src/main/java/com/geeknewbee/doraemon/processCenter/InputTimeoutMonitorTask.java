package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

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
    public static final int WAIT_SOUND_INPUT_OUT_TIME = 1000 * 10;
    public static final int EDD_TIME = 1000 * 5;
    public static final int WAIT_SOUND_END_TIME = 1000 * 18;

    private Context context;
    //上次输入时间
    private long begintTime;

    //是否正在运行
    private boolean isRunning = false;

    //检测超时的模式
    private TimeOutMonitorType model;

    public InputTimeoutMonitorTask(Context context) {
        this.context = context;
        begintTime = new Date().getTime();
    }

    private synchronized long getBeginTime() {
        return begintTime;
    }

    /**
     * 开始计算ASR监听时间
     *
     * @param type
     */
    public synchronized void startMonitor(TimeOutMonitorType type) {
        if (!isRunning)
            start();
        model = type;
        begintTime = new Date().getTime();
        LogUtils.d(TAG, "Start Timeout Monitor :" + type);
    }

    /**
     * 停止计算ASR监听时间
     */
    public synchronized void stopMonitor() {
        model = TimeOutMonitorType.MODEL_NONE;
        LogUtils.d(TAG, "Stop Timeout Monitor ...");
    }

    @Override
    public void run() {
        super.run();
        isRunning = true;
        while (true) {
            Date now = new Date();
            switch (model) {
                case MODEL_WAIT_SOUND_INPUT:
                    if (now.getTime() - getBeginTime() > WAIT_SOUND_INPUT_OUT_TIME) {
                        //当超过规定的时间要通知停止监听
                        stopMonitor();
                        EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                        Doraemon.getInstance(App.mContext).addCommand(new SoundCommand("不说话，我去休息了", SoundCommand.InputSource.TIPS));
                    }
                    break;
                case MODEL_WAIT_SOUND_END:
                    if (now.getTime() - getBeginTime() > WAIT_SOUND_END_TIME) {
                        //当超过规定的时间要通知停止监听
                        stopMonitor();
                        EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                        Doraemon.getInstance(App.mContext).addCommand(new SoundCommand("不说话，我去休息了", SoundCommand.InputSource.TIPS));
                    }
                    break;
                case MODEL_EDD_TIME:
                    if (now.getTime() - getBeginTime() > EDD_TIME) {
                        //当超过规定的时间要通知停止监听
                        stopMonitor();
                        EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                    }
                    break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static enum TimeOutMonitorType {
        MODEL_NONE,
        MODEL_WAIT_SOUND_INPUT,    // 1:等待说话超时
        MODEL_EDD_TIME, // 2:说话超时(现在防止在听的过程中sdk无法收到底层数据)
        MODEL_WAIT_SOUND_END  // 3:唤醒状态下定时执行唤醒(现在防止在唤醒状态sdk无法收到底层数据)
    }
}
