package com.sangebaba.doraemon.business.control;

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

    public CommandType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
