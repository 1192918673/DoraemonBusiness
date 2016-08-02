package com.geeknewbee.doraemon.input;


import com.geeknewbee.doraemon.control.base.IEye;

/**
 * ReadSense 实现 Eye
 */
public class ReadSenseEye implements IEye {
    private AFRListener afrListener;

    @Override
    public void startRecognition() {

    }

    @Override
    public void stopRecognition() {

    }

    @Override
    public void setAFRListener(AFRListener listener) {
        this.afrListener = listener;
    }
}
