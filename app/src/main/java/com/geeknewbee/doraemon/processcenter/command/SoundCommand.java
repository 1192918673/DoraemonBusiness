package com.geeknewbee.doraemon.processcenter.command;

/**
 * Created by ACER on 2016/8/23.
 */
public class SoundCommand extends Command {
    public InputSource inputSource;

    //是否覆盖正在tts的语音
    public boolean isOverwrite;

    public SoundCommand(String content, InputSource inputSource) {
        super(CommandType.PLAY_SOUND, content);
        this.inputSource = inputSource;
    }


    public static enum InputSource {
        //播放声音的来源 SOUND_TRANSLATE：解析音频，TTS后需要开启ARS  TIPS：播放些提示类的声音，播放完成不需要开启ASR
        // AFTER_WAKE_UP:唤醒成功后提醒专用  START_WAKE_UP:App开启后提醒语句，完成后才开启唤醒(否则存在被自己的声音唤醒的情况)
        //IOS_BUSINESS ios 商业端，需要给它回复TTS完成
        SOUND_TRANSLATE, TIPS, AFTER_WAKE_UP, START_WAKE_UP, IOS_BUSINESS
    }
}
