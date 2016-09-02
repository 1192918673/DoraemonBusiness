package com.geeknewbee.doraemon.output.action;

/**
 * 用于播放音乐、讲笑话
 */
public interface IMusicPlayer {
    boolean play(String param);

    boolean joke();

    boolean stop();

    void release();

    boolean isPlaying();

    void destroy();
}
