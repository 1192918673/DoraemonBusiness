
package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;

public class LimbActionCompleteEvent {
    public long commandId;
    public SportActionSetCommand.InputSource inputSource;

    public LimbActionCompleteEvent(long commandId, SportActionSetCommand.InputSource inputSource) {
        this.commandId = commandId;
        this.inputSource = inputSource;
    }

}
