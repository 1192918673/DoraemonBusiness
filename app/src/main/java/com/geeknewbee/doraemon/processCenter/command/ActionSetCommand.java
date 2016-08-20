package com.geeknewbee.doraemon.processcenter.command;

import java.util.ArrayList;
import java.util.List;

/**
 * 舞蹈命令
 */
public class ActionSetCommand extends Command {
    public List<SportAction> sportActions;

    public ActionSetCommand(List<SportAction> sportActions) {
        super(CommandType.ACTIONSET, "");
        this.sportActions = sportActions;
    }

    public ActionSetCommand() {
        super(CommandType.ACTIONSET);
        sportActions = new ArrayList<>();
    }

    public void addSportAction(List<SportAction> actions) {
        if (actions != null)
            sportActions.addAll(actions);
    }
}
