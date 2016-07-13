package com.sangebaba.doraemon.business.control;

public class Doraemon {
    private Doraemon() {
        ear = new AISpeechEar();
        eye = new ReadSenseEye();
    }

    private volatile static Doraemon instance;

    public static Doraemon getInstance() {
        if (instance == null) {
            synchronized (Doraemon.class) {
                if (instance == null) {
                    instance = new Doraemon();
                }
            }
        }
        return instance;
    }

    private IEar ear;
    private IEye eye;
    private ILimbs limbs;
    private IMouth mouth;

    /**
     * 开始自动声音识别 Automatic Speech Recognition
     */
    public void startASR() {
        ear.startRecognition();
    }

    /**
     * 停止自动语音识别
     */
    public void stopASR() {
        ear.stopRecognition();
    }

    /**
     * 开始自动人脸识别 Automatic face Recognition
     */
    public void startAFR() {
        eye.startRecognition();
    }

    /**
     * 停止自动人脸识别
     */
    public void stopAFR() {
        eye.stopRecognition();
    }

}
