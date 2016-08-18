package com.geeknewbee.doraemon.input;

/**
 * 思必驰声音输入板
 */
public class AISpeechDevice implements ISoundInputDevice {
    private boolean isWakeUp;

    @Override
    public void wakeUp() {

    }

    @Override
    public void sleep() {

    }

    @Override
    public void setAngle(int angle) {

    }

    @Override
    public boolean isWakeUp() {
        return true;
    }
}
