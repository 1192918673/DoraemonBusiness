package com.geeknewbee.doraemon.processcenter.command;

/**
 * Created by GYY on 2016/9/6.
 */
public class BLSPCommand extends Command {

    private String input;
    private String content;

    public BLSPCommand(String content, String input) {
        super(CommandType.BL_SP);
        this.content = content;
        this.input = input;
    }

    public String getInput() {
        return input;
    }

    @Override
    public String getContent() {
        return content;
    }
}
