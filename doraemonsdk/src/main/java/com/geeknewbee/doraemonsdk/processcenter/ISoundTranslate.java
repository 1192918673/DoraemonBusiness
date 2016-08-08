package com.geeknewbee.doraemonsdk.processcenter;

import com.geeknewbee.doraemonsdk.processcenter.command.Command;

import java.util.List;

/**
 * 声音string 转换为command
 */
public interface ISoundTranslate {
    void setTranslatorListener(OnTranslatorListener translatorListener);

    void translateSound(String s);

    interface OnTranslatorListener {
        /**
         * 声音翻译完成
         *
         * @param commands
         */
        void onTranslateComplete(List<Command> commands);
    }
}
