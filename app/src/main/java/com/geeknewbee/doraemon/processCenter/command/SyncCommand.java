package com.geeknewbee.doraemon.processcenter.command;

import java.util.ArrayList;
import java.util.List;

public class SyncCommand {
    private List<Command> commandList;
    private List<Long> unFinishIDS;

    public SyncCommand(List<Command> commandList) {
        this.commandList = commandList;
        unFinishIDS = new ArrayList<>();
        for (Command command : commandList) {
            unFinishIDS.add(command.getId());
        }
    }

    public List<Command> getCommandList() {
        return commandList;
    }

    /**
     * 命令是否已经全部完成
     *
     * @return
     */
    public boolean isComplete() {
        return unFinishIDS.size() == 0;
    }

    /**
     * 执行完成了一个Command
     *
     * @param id
     */
    public synchronized void executeComplete(long id) {
        unFinishIDS.remove(id);
    }
}
