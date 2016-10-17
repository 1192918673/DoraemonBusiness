package com.geeknewbee.doraemon.entity.event;

public class MusicCompleteEvent {
    public long commandId;

    public MusicCompleteEvent(long commandId) {
        this.commandId = commandId;
    }
}
