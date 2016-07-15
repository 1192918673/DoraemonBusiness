package com.sangebaba.doraemon.business.control;

/**
 * 声音命令翻译
 * 把声音翻译成不同的命令
 */
public class SoundTranslator {
    private OnTranslatorListener translatorListener;

    public void setTranslatorListener(OnTranslatorListener translatorListener) {
        this.translatorListener = translatorListener;
    }

    public void addTask(String s) {
        //TODO
    }


    interface OnTranslatorListener {
        /**
         * 声音翻译完成
         *
         * @param commands
         */
        void onTranslateComplete(Command... commands);
    }
}
