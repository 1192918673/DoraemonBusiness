package com.geeknewbee.doraemon.entity.event;

public class VideoCompleteEvent {
    public String commandId;

    public VideoCompleteEvent(String commandId) {
        this.commandId = commandId;
    }
}
