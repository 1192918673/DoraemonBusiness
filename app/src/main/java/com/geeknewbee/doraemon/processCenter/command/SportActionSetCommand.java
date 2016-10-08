package com.geeknewbee.doraemon.processcenter.command;

import java.util.ArrayList;
import java.util.List;

/**
 * 舞蹈命令
 */
public class SportActionSetCommand extends Command {
    public List<SportAction> sportActions;

    public SportActionSetCommand(List<SportAction> sportActions) {
        super(CommandType.ACTIONSET, "");
        this.sportActions = sportActions;
    }

    public SportActionSetCommand() {
        super(CommandType.ACTIONSET);
        sportActions = new ArrayList<>();
    }

    public void addSportAction(List<SportAction> actions) {
        if (actions != null)
            sportActions.addAll(actions);
    }
}
