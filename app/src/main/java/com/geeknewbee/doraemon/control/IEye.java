package com.geeknewbee.doraemon.control;

/**
 * 眼睛
 * 用于实现人脸识别、拍照
 */
public interface IEye {
    /**
     * 开始语音识别
     */
    void startRecognition();

    /**
     * 停止语音识别
     */
    void stopRecognition();

    void setAFRListener(AFRListener listener);

    interface AFRListener {
        void onDetectFace();
    }
}
