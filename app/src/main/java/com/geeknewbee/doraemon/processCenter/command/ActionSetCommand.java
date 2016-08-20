package com.geeknewbee.doraemon.processcenter.command;

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
}
