package com.geeknewbee.doraemonsdk.output.action;

/**
 * 文字转语音
 * 用于发音
 */
public interface ITTS {
    boolean talk(String param);

    boolean stop();
}
