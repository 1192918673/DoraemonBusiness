package com.geeknewbee.doraemon.input;

/**
 * 眼睛
 * 用于实现人脸识别、拍照
 */
public interface IEye {
    /**
     * 开始人脸识别
     */
    void startRecognition();

    /**
     * 停止人脸识别
     */
    void stopRecognition();

    void setAFRListener(AFRListener listener);

    interface AFRListener {
        void onDetectFace();
    }
}
