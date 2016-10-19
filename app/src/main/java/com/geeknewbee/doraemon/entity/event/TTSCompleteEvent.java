package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.processcenter.command.SoundCommand;

public class TTSCompleteEvent {
    public SoundCommand.InputSource inputSource;
    private long id;
    public boolean isSuccess;
    public String error;

    public long getTTSCommandID() {
        return id;
    }

    public TTSCompleteEvent(SoundCommand.InputSource inputSource, long id, boolean isSuccess, String error) {
        this.inputSource = inputSource;
        this.id = id;
        this.isSuccess = isSuccess;
        this.error = error;
    }

}
