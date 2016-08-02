package com.geeknewbee.doraemon.control;

/**
 * 文字转语音
 * 用于发音
 */
public interface ITTS {
    boolean talk(String param);

    boolean stop();
}
