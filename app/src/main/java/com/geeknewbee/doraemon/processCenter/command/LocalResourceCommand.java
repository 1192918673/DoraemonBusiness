package com.geeknewbee.doraemon.processcenter.command;

public class LocalResourceCommand extends Command {
    public int resourceID;

    public LocalResourceCommand(int resourceID) {
        super(CommandType.PLAY_LOCAL_RESOURCE);
        this.resourceID = resourceID;
    }
}
