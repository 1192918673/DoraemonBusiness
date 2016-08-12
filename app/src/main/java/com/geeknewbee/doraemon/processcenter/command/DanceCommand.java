package com.geeknewbee.doraemon.processcenter.command;

import java.util.List;

/**
 * 舞蹈命令
 */
public class DanceCommand extends Command {
    public List<DanceAction> danceActions;

    public DanceCommand(List<DanceAction> danceActions) {
        super(CommandType.DANCE, "");
        this.danceActions = danceActions;
    }
}
