package com.geeknewbee.doraemon.entity.event;

public class ASRResultEvent {
    /**
     * ASR解析是否成功
     */
    public boolean isSuccess;

    public boolean isFromPhone;
    public String input;
    public String asrOutput;
    public String action;
    public String starName;
    public String musicName;

    public ASRResultEvent(boolean isSuccess, boolean isFromPhone, String input, String asrOutput, String action, String starName, String musicName) {
        this.isSuccess = isSuccess;
        this.isFromPhone = isFromPhone;
        this.input = input;
        this.asrOutput = asrOutput;
        this.action = action;
        this.starName = starName;
        this.musicName = musicName;
    }
}
