package com.geeknewbee.doraemon.processcenter.command;

/**
 * Created by ACER on 2016/8/23.
 */
public class SoundCommand extends Command {
    public InputSource inputSource;

    public SoundCommand(String content, InputSource inputSource) {
        super(CommandType.PLAY_SOUND, content);
        this.inputSource = inputSource;
    }


    public static enum InputSource {
        //播放声音的来源 SOUND_TRANSLATE：解析音频，TTS后需要开启ARS  TIPS：播放些提示类的声音，播放完成不需要开启ASR
        SOUND_TRANSLATE, TIPS
    }
}
