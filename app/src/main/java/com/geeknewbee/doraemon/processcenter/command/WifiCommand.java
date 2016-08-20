package com.geeknewbee.doraemon.processcenter.command;

public class WifiCommand extends Command {
    public String ssid;
    public String pwd;
    public int type;

    public WifiCommand(String ssid, String pwd, int type) {
        super(CommandType.WIFI_MESSAGE);
        this.ssid = ssid;
        this.pwd = pwd;
        this.type = type;
    }
}
