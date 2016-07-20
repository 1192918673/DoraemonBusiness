package com.geeknewbee.doraemon.control.base;

/**
 * 嘴
 * 用于发音
 */
public interface IMouth {
    boolean talk(String param);

    boolean stop();
}
