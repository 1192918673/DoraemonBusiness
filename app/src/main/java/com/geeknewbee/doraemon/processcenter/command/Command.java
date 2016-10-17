package com.geeknewbee.doraemon.processcenter.command;

import com.geeknewbee.doraemon.constants.Constants;

/**
 * Brain执行的命令
 */
public class Command {
    private long id;
    private CommandType type;
    private String content;

    public Command(CommandType type, String content) {
        this.type = type;
        this.content = content;
        id = System.currentTimeMillis();
    }

    public Command(CommandType type) {
        this(type, Constants.EMPTY_STRING);
    }

    public CommandType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "command type:" + type.ordinal() + " content:" + content;
    }

}
