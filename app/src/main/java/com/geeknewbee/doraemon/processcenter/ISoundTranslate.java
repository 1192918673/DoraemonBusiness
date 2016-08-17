package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.processcenter.command.Command;
import java.util.List;

/**
 * 声音string 转换为command
 */
public interface ISoundTranslate {

    void setTranslatorListener(OnTranslatorListener translatorListener);

    void translateSound(SoundTranslateInput input);

    interface OnTranslatorListener {
        /**
         * 声音翻译完成
         *
         * @param commands
         */
        void onTranslateComplete(List<Command> commands);
    }
}
