package com.geeknewbee.doraemon.processcenter.command;

import com.geeknewbee.doraemon.constants.Constants;

import java.util.UUID;

/**
 * Brain执行的命令
 */
public class Command {
    private String id;
    private CommandType type;
    private String content;

    public Command(CommandType type, String content) {
        this.type = type;
        this.content = content;
        id = UUID.randomUUID().toString();
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

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "command type:" + type.ordinal() + " content:" + content;
    }

}
