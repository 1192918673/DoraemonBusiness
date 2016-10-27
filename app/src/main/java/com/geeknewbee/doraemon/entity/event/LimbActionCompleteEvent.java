
package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;

public class LimbActionCompleteEvent {
    public String commandId;
    public SportActionSetCommand.InputSource inputSource;

    public LimbActionCompleteEvent(String commandId, SportActionSetCommand.InputSource inputSource) {
        this.commandId = commandId;
        this.inputSource = inputSource;
    }

}
