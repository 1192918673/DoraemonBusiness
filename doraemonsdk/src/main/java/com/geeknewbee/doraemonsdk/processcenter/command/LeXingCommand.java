package com.geeknewbee.doraemonsdk.processcenter.command;

public class LeXingCommand extends Command {
    public LeXingCommand() {
        super(CommandType.LE_XING_FOOT, "");
    }

    public LeXingCommand(int v, int w) {
        this();
        this.v = v;
        this.w = w;
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
