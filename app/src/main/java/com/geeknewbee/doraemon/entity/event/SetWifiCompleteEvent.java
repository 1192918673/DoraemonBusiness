package com.geeknewbee.doraemon.entity.event;

public class SetWifiCompleteEvent {

    public boolean isSuccess;
    public String SSID;
    //把当前的MAC地址发送给手机
    public String content;
    //是否已经绑定
    public boolean hadBound;
    public String ipAddress;

    public SetWifiCompleteEvent(boolean isSuccess, String ssid) {
        this.isSuccess = isSuccess;
        this.SSID = ssid;
    }

    public SetWifiCompleteEvent(boolean isSuccess, boolean hadBound, String SSID, String content, String ipAddress) {
        this.isSuccess = isSuccess;
        this.SSID = SSID;
        this.content = content;
        this.hadBound = hadBound;
        this.ipAddress = ipAddress;
    }

}
