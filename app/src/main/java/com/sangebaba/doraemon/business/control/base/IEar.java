package com.sangebaba.doraemon.business.control.base;

/**
 * 耳朵
 * 用于识别语音
 **/
public interface IEar {
    /**
     * 开始语音识别
     */
    void startRecognition();

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

    interface ASRListener {
        void onASRResult(String originSoundString, String outputString);
    }
}
