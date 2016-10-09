package com.geeknewbee.doraemon.processcenter.command;

import java.util.ArrayList;
import java.util.List;

/**
 * 舞蹈命令
 */
public class SportActionSetCommand extends Command {
    public List<SportAction> sportActions;

    //是否覆盖正在tts的语音
    public boolean isOverwrite;

    public SportActionSetCommand(List<SportAction> sportActions) {
        super(CommandType.SPORT_ACTION_SET, "");
        this.sportActions = sportActions;
    }

    public SportActionSetCommand(List<SportAction> sportActions, boolean isOverwrite) {
        super(CommandType.SPORT_ACTION_SET, "");
        this.sportActions = sportActions;
        this.isOverwrite = isOverwrite;
    }

    public SportActionSetCommand() {
        super(CommandType.SPORT_ACTION_SET);
        sportActions = new ArrayList<>();
    }

    public void addSportAction(List<SportAction> actions) {
        if (actions != null)
            sportActions.addAll(actions);
    }
}
