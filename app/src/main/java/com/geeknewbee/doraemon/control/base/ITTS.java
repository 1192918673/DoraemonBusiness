package com.geeknewbee.doraemon.control.base;

/**
 * 嘴
 * 用于发音
 */
public interface ITTS {
    boolean talk(String param);

    boolean stop();
}
