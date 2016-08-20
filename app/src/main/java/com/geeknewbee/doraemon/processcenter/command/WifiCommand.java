package com.geeknewbee.doraemon.processcenter.command;

/**
 * 设置Wifi命令
 */
public class WifiCommand extends Command {

    public int encryptType; //1 无密码，2 WEB加密, 3 WPA加密
    public String SSID;
    public String pwd;

    public WifiCommand() {
        super(CommandType.WIFI_MESSAGE, "");
    }

    public WifiCommand(int encryptType, String SSID, String pwd) {
        this();
        this.encryptType = encryptType;
        this.SSID = SSID;
        this.pwd = pwd;
    }
}
