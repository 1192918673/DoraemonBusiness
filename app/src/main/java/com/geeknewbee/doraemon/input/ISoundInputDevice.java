package com.geeknewbee.doraemon.input;

/**
 * 耳朵
 * 用于接受唤醒词
 */
public interface ISoundInputDevice {

    boolean reInit();

    /**
     * 开启唤醒监听
     */
    void start();

    /**
     * 关闭唤醒监听
     */
    void stop();

    void setAngle(int angle);

    void onWakeUp(double angle);
}
