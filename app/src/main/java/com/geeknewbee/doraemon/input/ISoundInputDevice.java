package com.geeknewbee.doraemon.input;

/**
 * 声音输入控制硬件
 */
public interface ISoundInputDevice {

    /**
     * 开启唤醒监听
     */
    void start();

    /**
     * 关闭唤醒监听
     */
    void stop();

    void sleep();

    void setAngle(int angle);

    void onWakeUp(double angle);
}
