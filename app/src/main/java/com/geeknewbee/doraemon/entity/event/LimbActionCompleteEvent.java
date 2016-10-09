
package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;

public class LimbActionCompleteEvent {
    public SportActionSetCommand.InputSource inputSource;

    public LimbActionCompleteEvent(SportActionSetCommand.InputSource inputSource) {
        this.inputSource = inputSource;
    }
}
