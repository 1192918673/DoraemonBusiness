package com.geeknewbee.doraemon.input;

/**
 * 耳朵
 * 用于识别语音
 **/
public interface IEar {

    boolean reInit();

    boolean isListening();

    /**
     * 开始语音识别
     */
    void startRecognition(double phis);

    /**
     * 停止语音识别
     */
    void stopRecognition();

    /**
     * 设置语音识别listener
     *
     * @param listener
     */
    void setASRListener(ASRListener listener);

    void destroy();

    interface ASRListener {
        void onASRResult(String input, String asrOutput, String action, String starName, String musicName);
    }
}
