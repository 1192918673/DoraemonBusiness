package com.sangebaba.doraemon.business.control;

import com.sangebaba.doraemon.business.task.SoundTranslateTask;
import com.sangebaba.doraemon.business.util.Constant;
import com.sangebaba.doraemon.business.util.LogUtils;

import java.util.List;

/**
 * 声音命令翻译
 * 把声音翻译成不同的命令
 */
public class SoundTranslator {
    private OnTranslatorListener translatorListener;

    public void setTranslatorListener(OnTranslatorListener translatorListener) {
        this.translatorListener = translatorListener;
    }

    public synchronized void addTask(String s) {
        LogUtils.d(Constant.TAG_COMMAND, "add sound translate task:" + s);

        new SoundTranslateTask(translatorListener).execute(s);
    }


    public interface OnTranslatorListener {
        /**
         * 声音翻译完成
         *
         * @param commands
         */
        void onTranslateComplete(List<Command> commands);
    }
}
