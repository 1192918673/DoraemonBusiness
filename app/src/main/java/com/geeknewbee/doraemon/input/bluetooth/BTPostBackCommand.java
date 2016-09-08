package com.geeknewbee.doraemon.input.bluetooth;

/**
 * 回传给手机的蓝牙命令格式
 */
public class BTPostBackCommand {
    private SetWIFICallBack wifiCallBack;

    public void setWifiCallBack(SetWIFICallBack wifiCallBack) {
        this.wifiCallBack = wifiCallBack;
    }

    public static class SetWIFICallBack {
        public boolean isSuccess;
        public boolean hadBound;
        public String content;
    }
}
