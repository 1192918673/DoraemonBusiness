package com.geeknewbee.doraemon.output.action;

import android.content.Context;

/**
 * 视频播放接口
 */
public interface IVideoPlayer {
    boolean init();

    void play(Context context, String url);

    void stop();

    boolean isPlaying();
}
