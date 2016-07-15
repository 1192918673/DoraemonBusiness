package com.sangebaba.doraemon.business.task;

import com.sangebaba.doraemon.business.control.Command;
import com.sangebaba.doraemon.business.control.SoundTranslator;
import com.sangebaba.doraemon.business.task.base.Priority;
import com.sangebaba.doraemon.business.task.base.PriorityTask;

import java.util.List;

/**
 * 声音翻译task
 */
public class SoundTranslateTask extends PriorityTask<String, Void, List<Command>> {
    private SoundTranslator.OnTranslatorListener translatorListener;

    public SoundTranslateTask(SoundTranslator.OnTranslatorListener translatorListener) {
        this(Priority.DEFAULT, translatorListener);
    }

    public SoundTranslateTask(Priority priority, SoundTranslator.OnTranslatorListener translatorListener) {
        super(priority);
        this.translatorListener = translatorListener;
    }

    @Override
    protected List<Command> performTask(String... params) {
        //TODO 网络请求
        return null;
    }

    @Override
    protected void onPostExecute(List<Command> commands) {
        super.onPostExecute(commands);
        translatorListener.onTranslateComplete(commands);
    }
}
