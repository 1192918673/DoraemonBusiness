package com.geeknewbee.doraemon.control.base;

/**
 * 文字转语音
 * 用于发音
 */
public interface ITTS {
    boolean talk(String param);

    boolean stop();
}
