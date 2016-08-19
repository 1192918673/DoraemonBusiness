package com.geeknewbee.doraemon.input.bluetooth;

import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.WifiCommand;

public class BLECommand {
    //1：设置wifi  0：绑定成功
    public int type;
    public String ssid;
    public String pdw;
    public int wifttype;

    public Command getCommand() {
        switch (type) {
            case 1:
                return new WifiCommand(ssid, pdw, wifttype);
            case 0:
                return new Command(CommandType.BIND_ACCOUNT_SUCCESS);
        }
        return null;
    }
}
