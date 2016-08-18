package com.geeknewbee.doraemon.processcenter.command;

public class ExpressionCommand extends Command {

    public final int loops;

    public ExpressionCommand(String content, int loops) {
        super(CommandType.SHOW_EXPRESSION, content);
        this.loops = loops;
    }
}
