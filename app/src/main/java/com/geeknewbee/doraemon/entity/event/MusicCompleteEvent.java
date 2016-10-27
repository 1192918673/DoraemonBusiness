package com.geeknewbee.doraemon.entity.event;

public class MusicCompleteEvent {
    public String commandId;

    public MusicCompleteEvent(String commandId) {
        this.commandId = commandId;
    }
}
