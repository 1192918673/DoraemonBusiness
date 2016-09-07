package com.geeknewbee.doraemon.entity.event;

public class ASRResultEvent {
    /**
     * ASR解析是否成功
     */
    public boolean isSuccess;

    public ASRResultEvent(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}
