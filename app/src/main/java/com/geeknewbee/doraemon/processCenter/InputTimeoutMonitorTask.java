package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.Date;

/**
 * 输入监听task
 * 当经历定义的时间后没有输入 则停止声音监听，等待唤醒
 */
public class InputTimeoutMonitorTask extends Thread {

    public static final String TAG = AISpeechEar.class.getSimpleName();
    //超时时间
    public static final int WAIT_SOUND_INPUT_OUT_TIME = 1000 * 15;

    private Context context;
    //上次输入时间
    private long beginTime;

    //检测超时的模式
    private TimeOutMonitorType model;
    private boolean isExit;

    public InputTimeoutMonitorTask(Context context) {
        this.context = context;
        beginTime = new Date().getTime();
    }

    /**
     * 开始计算ASR监听时间
     *
     * @param type
     */
    public synchronized void startMonitor(TimeOutMonitorType type) {
        beginTime = new Date().getTime();
        model = type;
        if (!isAlive()) {
            isExit = false;
            start();
        }
        LogUtils.d(TAG, "Start Timeout Monitor :" + type);
    }

    /**
     * 停止计算ASR监听时间
     */
    public synchronized void stopMonitor() {
        model = TimeOutMonitorType.MODEL_NONE;
        LogUtils.d(TAG, "Stop Timeout Monitor ...");
    }

    public void cancel() {
        isExit = true;
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        Date now;
        while (!isExit) {
            now = new Date();
            switch (model) {
                case MODEL_WAIT_SOUND_INPUT:
                    if (now.getTime() - beginTime > WAIT_SOUND_INPUT_OUT_TIME) {
                        //当超过规定的时间要通知停止监听
                        stopMonitor();
                        //添加Sound Command 就会开启到EDD模式，不需要手动切换到EDD
                        Doraemon.getInstance(App.mContext).addCommand(new SoundCommand("不说话，我去休息了", SoundCommand.InputSource.TIPS));
                    }
                    break;
                default:
                    break;
            }
            //这里测试蓝牙4.0经常找不到的问题 能否激活
            DeviceUtil.closeDiscoverableTimeout();
            DeviceUtil.setDiscoverableTimeout(1000 * 60 * 60);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public enum TimeOutMonitorType {
        MODEL_NONE,
        MODEL_WAIT_SOUND_INPUT    // 1:等待说话超时
    }
}
