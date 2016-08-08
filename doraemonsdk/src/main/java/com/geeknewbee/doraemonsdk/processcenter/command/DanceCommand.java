package com.geeknewbee.doraemonsdk.processcenter.command;

import java.util.List;

/**
 * 舞蹈命令
 */
public class DanceCommand extends Command {
    public DanceCommand(List<DanceAction> danceActions) {
        super(CommandType.DANCE, "");
        this.danceActions = danceActions;
    }

    public List<DanceAction> danceActions;
}
