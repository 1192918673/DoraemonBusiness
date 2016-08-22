package com.geeknewbee.doraemon.input;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.Doraemon;

/**
 * 思必驰声音输入板
 */
public class AISpeechSoundInputDevice implements ISoundInputDevice {

    /**
     * 停止监听输入
     */
    @Override
    public void sleep() {
        //这里就是停止语音监听 唤醒的引擎一直开着
        Doraemon.getInstance(App.mContext).stopASR();
    }

    @Override
    public void setAngle(int angle) {

    }

    @Override
    public synchronized void onWakeUp(int angle) {
        //当唤醒的时候停止当前的动作
        MouthTaskQueue.getInstance().stop();
        LimbsTaskQueue.getInstance().stop();
        //开启监听
        Doraemon.getInstance(App.mContext).startASR();
        //TODO 根据角度转向
    }

}
