package com.geeknewbee.doraemon.entity.event;

public class SetWifiCompleteEvent {

    public SetWifiCompleteEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess;
}
