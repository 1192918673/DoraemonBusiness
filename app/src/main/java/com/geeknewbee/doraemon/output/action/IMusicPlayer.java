package com.geeknewbee.doraemon.output.action;

import com.geeknewbee.doraemon.processcenter.command.Command;

/**
 * 用于播放音乐、讲笑话
 */
public interface IMusicPlayer {

    void reInit();

    boolean play(Command command);

    boolean joke(Command command);

    boolean stop();

    void release();

    boolean isPlaying();

    void destroy();
}
