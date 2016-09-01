package com.geeknewbee.doraemon.entity.event;

public class NetWorkStateChangeEvent {
    public boolean isConnected;

    public NetWorkStateChangeEvent(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
