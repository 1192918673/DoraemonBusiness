package com.geeknewbee.doraemon.entity;

public class SoundTranslateInput {

    // 输入的语音 string
    public String input;
    // 第三方语音解析库 返回的 output
    public String asrOutput;

    public SoundTranslateInput(String input, String asrOutput) {
        this.input = input;
        this.asrOutput = asrOutput;
    }
}
