package com.geeknewbee.doraemon.entity.event;

public class FaceControlCompleteEvent {
    private String id;

    public FaceControlCompleteEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
