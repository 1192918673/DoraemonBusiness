package com.geeknewbee.doraemon.input;

/**
 * 声音输入控制硬件
 */
public interface ISoundInputDevice {
    void wakeUp();

    void sleep();

    void setAngle(int angle);

    boolean isWakeUp();
}
