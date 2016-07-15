package com.sangebaba.doraemon.business.control;

import com.sangebaba.doraemon.business.control.base.ILimbs;
import com.sangebaba.doraemon.business.control.base.IMouth;
import com.sangebaba.doraemon.business.task.MouthTaskQueue;
import com.sangebaba.doraemon.business.task.base.Priority;

/**
 * 大脑中枢
 * 对声音、人脸输入 进行各种响应
 * 输出行为有 语音| 四肢运动|显示表情|播放电影等
 * 输出终端有 喇叭/肢体/屏幕等。 每个终端保持一个 priority queue，每个终端的task任务必须串行。
 */
public class Brain implements SoundTranslator.OnTranslatorListener {
    private ILimbs limbs;
    private IMouth mouth;
    private SoundTranslator soundTranslator;

    /**
     * 喇叭终端队列
     */
    private MouthTaskQueue mouthTaskQueue;

    public Brain(IMouth mouth, ILimbs limbs) {
        this.limbs = limbs;
        this.mouth = mouth;

        soundTranslator = new SoundTranslator();
        soundTranslator.setTranslatorListener(this);

        mouthTaskQueue = new MouthTaskQueue();
        MouthTaskQueue.setMouth(mouth);
    }

    public void translateSound(String s) {
        soundTranslator.addTask(s);
    }

    public void addCommand(Command command) {
        switch (command.getType()) {
            case PLAY_SOUND:
                MouthTaskQueue.addTask(Priority.DEFAULT, command.getContent());
                break;
            case MECHANICAL_MOVEMENT:
                break;
            case SHOW_EXPRESSION:
                break;
        }
    }

    @Override
    public void onTranslateComplete(Command... commands) {
        for (Command command : commands) {
            addCommand(command);
        }
    }
}
