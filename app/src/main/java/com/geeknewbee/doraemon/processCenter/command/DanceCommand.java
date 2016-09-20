package com.geeknewbee.doraemon.processcenter.command;

import java.util.ArrayList;
import java.util.List;

/**
 * 舞蹈命令
 */
public class DanceCommand extends Command {
    public List<SportAction> sportActions;

    public DanceCommand(List<SportAction> sportActions) {
        super(CommandType.DANCE, "");
        this.sportActions = sportActions;
    }

    public DanceCommand() {
        super(CommandType.DANCE);
        sportActions = new ArrayList<>();
    }

    public void addSportAction(List<SportAction> actions) {
        if (actions != null)
            sportActions.addAll(actions);
    }
}
