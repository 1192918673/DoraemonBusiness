package com.geeknewbee.doraemon.processcenter.command;

public class LeXingCommand extends Command {
    public LeXingCommand() {
        super(CommandType.LE_XING_FOOT, "");
    }

    /**
     * 线速度
     */
    public int v;
    /**
     * 角速度
     */
    public int w;
}
