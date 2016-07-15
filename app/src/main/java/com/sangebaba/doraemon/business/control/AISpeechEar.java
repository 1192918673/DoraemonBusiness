package com.sangebaba.doraemon.business.control;

import com.sangebaba.doraemon.business.control.base.IEar;

/**
 * 思必驰 实现 ear
 */
public class AISpeechEar implements IEar {
    private ASRListener asrListener;

    @Override
    public void startRecognition() {

    }

    @Override
    public void stopRecognition() {

    }

    @Override
    public void setASRListener(ASRListener listener) {
        this.asrListener = listener;
    }
}
