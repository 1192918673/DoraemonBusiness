package com.geeknewbee.doraemon.output.action;

/**
 * 文字转语音
 * 用于发音
 */
public interface ITTS {
    boolean talk(String param);

    boolean stop();

    void setTTSListener(TTSListener listener);

    public interface TTSListener {
        void onComplete();
    }
}
