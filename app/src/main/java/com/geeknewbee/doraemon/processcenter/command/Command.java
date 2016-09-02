package com.geeknewbee.doraemon.processcenter.command;

import com.geeknewbee.doraemon.constants.Constants;

/**
 * Brain执行的命令
 */
public class Command {

    private CommandType type;
    private String content;

    public Command(CommandType type, String content) {
        this.type = type;
        this.content = content;
    }

    public Command(CommandType type) {
        this.type = type;
        this.content = Constants.EMPTY_STRING;
    }

    public CommandType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "command type:" + type.ordinal() + " content:" + content;
    }

}
