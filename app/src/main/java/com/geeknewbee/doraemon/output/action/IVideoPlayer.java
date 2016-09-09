package com.geeknewbee.doraemon.output.action;

/**
 * 视频播放接口
 */
public interface IVideoPlayer {
    boolean init();

    void play(String url);

    void stop();

    boolean isPlaying();
}
