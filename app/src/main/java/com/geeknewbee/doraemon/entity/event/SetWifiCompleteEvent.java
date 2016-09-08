package com.geeknewbee.doraemon.entity.event;

public class SetWifiCompleteEvent {

    public boolean isSuccess;
    public String SSID;
    //把当前的MAC地址发送给手机
    public String content;
    //是否已经绑定
    public boolean hadBound;

    public SetWifiCompleteEvent(boolean isSuccess, String ssid) {
        this.isSuccess = isSuccess;
        this.SSID = ssid;
    }

    public SetWifiCompleteEvent(boolean isSuccess, String SSID, String content) {
        this.isSuccess = isSuccess;
        this.SSID = SSID;
        this.content = content;
    }

    public SetWifiCompleteEvent(boolean isSuccess, String SSID, boolean hadBound) {
        this.isSuccess = isSuccess;
        this.SSID = SSID;
        this.hadBound = hadBound;
    }
}
