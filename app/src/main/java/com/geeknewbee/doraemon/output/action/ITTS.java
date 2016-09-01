package com.geeknewbee.doraemon.output.action;

import com.geeknewbee.doraemon.processcenter.command.SoundCommand;

/**
 * 文字转语音
 * 用于发音
 */
public interface ITTS {

    boolean reInit();
    boolean isSpeaking();

    boolean talk(String param, SoundCommand.InputSource inputSource);

    boolean stop();
}
