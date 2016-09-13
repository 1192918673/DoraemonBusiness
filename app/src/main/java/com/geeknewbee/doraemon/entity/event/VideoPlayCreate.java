package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.output.action.IVideoPlayer;

public class VideoPlayCreate {
    public IVideoPlayer videoPlayer;

    public VideoPlayCreate(IVideoPlayer videoPlayer) {
        this.videoPlayer = videoPlayer;
    }
}
