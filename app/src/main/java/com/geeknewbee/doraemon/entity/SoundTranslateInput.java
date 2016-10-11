package com.geeknewbee.doraemon.entity;

public class SoundTranslateInput {

    // 输入的语音 string
    public String input;
    // 第三方语音解析库 返回的 asrOutput
    public String asrOutput;
    // 动作类型
    public String action;
    // 歌手名
    public String starName;
    // 歌曲名
    public String musicName;

    public SoundTranslateInput(String input, String asrOutput, String action, String starName, String musicName) {
        this.input = input;
        this.asrOutput = asrOutput;
        this.action = action;
        this.starName = starName;
        this.musicName = musicName;
    }
}
