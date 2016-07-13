package com.sangebaba.doraemon.business.control;

public class Doraemon implements IEar.ASRListener, IEye.AFRListener {
    private volatile static Doraemon instance;
    private IEar ear;
    private IEye eye;
    private ILimbs limbs;
    private IMouth mouth;
    private Doraemon() {
        ear = new AISpeechEar();
        eye = new ReadSenseEye();
    }

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

    /**
     * 开始自动声音识别 Automatic Speech Recognition
     */
    public void startASR() {
        ear.startRecognition();
        ear.setASRListener(this);
    }

    /**
     * 停止自动语音识别
     */
    public void stopASR() {
        ear.setASRListener(null);
        ear.stopRecognition();
    }

    /**
     * 开始自动人脸识别 Automatic face Recognition
     */
    public void startAFR() {
        eye.startRecognition();
        eye.setAFRListener(this);
    }

    /**
     * 停止自动人脸识别
     */
    public void stopAFR() {
        eye.setAFRListener(null);
        eye.stopRecognition();
    }


    /**
     * 语音识别结果
     *
     * @param result
     */
    @Override
    public void onASRResult(String result) {

    }

    /**
     * 检测到人脸
     */
    @Override
    public void onDetectFace() {

    }
}
