package com.geeknewbee.doraemon.control;

/**
 * 用于播放音乐
 */
public interface IMusicPlayer {
    boolean play(String param);

    boolean stop();

    void release();
}
