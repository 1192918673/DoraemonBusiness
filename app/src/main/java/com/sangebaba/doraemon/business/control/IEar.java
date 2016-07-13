package com.sangebaba.doraemon.business.control;

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
}
