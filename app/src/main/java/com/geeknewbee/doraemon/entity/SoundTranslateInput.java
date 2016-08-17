package com.geeknewbee.doraemon.entity;

public class SoundTranslateInput {

    // 输入的语音 string
    public String input;
    // 第三方语音解析库 返回的 output
    public String asrOutput;
    // 歌手名
    public String starName;
    // 歌曲名
    public String musicName;

    public SoundTranslateInput(String input, String asrOutput, String starName, String musicName) {
        this.input = input;
        this.asrOutput = asrOutput;
        this.starName = starName;
        this.musicName = musicName;
    }
}
