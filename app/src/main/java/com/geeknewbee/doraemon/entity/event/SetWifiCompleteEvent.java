package com.geeknewbee.doraemon.entity.event;

public class SetWifiCompleteEvent {

    public boolean isSuccess;
    public String SSID;

    public SetWifiCompleteEvent(boolean isSuccess, String ssid) {
        this.isSuccess = isSuccess;
        this.SSID = ssid;
    }
}
