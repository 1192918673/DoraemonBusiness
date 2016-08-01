package com.geeknewbee.doraemon.control;

import com.geeknewbee.doraemon.model.DanceAction;

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
