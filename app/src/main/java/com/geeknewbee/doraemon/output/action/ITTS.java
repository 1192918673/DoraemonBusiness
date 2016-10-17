package com.geeknewbee.doraemon.output.action;

import com.geeknewbee.doraemon.processcenter.command.SoundCommand;

/**
 * 文字转语音
 * 用于发音
 */
public interface ITTS {

    boolean reInit();

    boolean isBusy();

    boolean talk(SoundCommand command);

    boolean stop();

    void addSoundCommand(SoundCommand command);

    void destroy();
}
