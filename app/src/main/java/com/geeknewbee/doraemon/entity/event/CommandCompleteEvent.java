package com.geeknewbee.doraemon.entity.event;

public class CommandCompleteEvent {
    private String id;

    public CommandCompleteEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
