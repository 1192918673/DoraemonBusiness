package com.geeknewbee.doraemon.processcenter.command;

/**
 * Created by GYY on 2016/9/6.
 */
public class BLSPCommand extends Command {

    private String input;
    private String mac;

    public BLSPCommand(String content, String input) {
        super(CommandType.BL_SP);
        this.mac = content;
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    public String getMac() {
        return mac;
    }
}
