package com.geeknewbee.doraemon.entity.event;

public class ASRResultEvent {
    /**
     * ASR解析是否成功
     */
    public boolean isSuccess;

    public String input;
    public String asrOutput;
    public String action;
    public String starName;
    public String musicName;

    public ASRResultEvent(boolean isSuccess, String input, String asrOutput, String action, String starName, String musicName) {
        this.isSuccess = isSuccess;
        this.input = input;
        this.asrOutput = asrOutput;
        this.action = action;
        this.starName = starName;
        this.musicName = musicName;
    }
}
