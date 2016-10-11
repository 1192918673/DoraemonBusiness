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


    public InputSource inputSource = InputSource.INTERNAL;

    public SportActionSetCommand(List<SportAction> sportActions) {
        super(CommandType.SPORT_ACTION_SET, "");
        this.sportActions = sportActions;
    }

    public SportActionSetCommand(List<SportAction> sportActions, boolean isOverwrite, InputSource source) {
        super(CommandType.SPORT_ACTION_SET, "");
        this.sportActions = sportActions;
        this.isOverwrite = isOverwrite;
        this.inputSource = source;
    }

    public SportActionSetCommand(InputSource source) {
        super(CommandType.SPORT_ACTION_SET);
        this.inputSource = source;
        sportActions = new ArrayList<>();
    }

    public SportActionSetCommand() {
        super(CommandType.SPORT_ACTION_SET);
        sportActions = new ArrayList<>();
    }

    public void addSportAction(List<SportAction> actions) {
        if (actions != null)
            sportActions.addAll(actions);
    }

    public enum InputSource {
        //命令的来源,INTERNAL:内部  REMOTE_CONTROL:远程控制（完成后不需要开始ASR）
        INTERNAL, REMOTE_CONTROL
    }
}
