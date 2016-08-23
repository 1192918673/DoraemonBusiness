package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.processcenter.command.SoundCommand;

public class TTSCompleteEvent {
    public SoundCommand.InputSource inputSource;

    public TTSCompleteEvent(SoundCommand.InputSource inputSource) {
        this.inputSource = inputSource;
    }
}
