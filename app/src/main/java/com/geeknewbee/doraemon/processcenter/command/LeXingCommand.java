package com.geeknewbee.doraemon.processcenter.command;

public class LeXingCommand extends Command {
    /**
     * 线速度
     */
    public int v;
    /**
     * 角速度
     */
    public int w;

    public LeXingCommand() {
        super(CommandType.LE_XING_FOOT, "");
    }

    public LeXingCommand(int v, int w) {
        this();
        this.v = v;
        this.w = w;
    }
}
