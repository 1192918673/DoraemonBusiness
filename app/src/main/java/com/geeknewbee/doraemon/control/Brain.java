package com.geeknewbee.doraemon.control;

import com.geeknewbee.doraemon.control.base.ILimbs;
import com.geeknewbee.doraemon.task.FaceManager;
import com.geeknewbee.doraemon.task.LimbTaskQueue;
import com.geeknewbee.doraemon.task.MouthTaskQueue;
import com.geeknewbee.doraemon.task.base.Priority;
import com.geeknewbee.doraemon.util.Constant;
import com.geeknewbee.doraemon.util.LogUtils;

import java.util.List;

/**
 * 大脑中枢
 * 对声音、人脸输入 进行各种响应
 * 输出行为有 语音| 四肢运动|显示表情|播放电影等
 * 输出终端有 喇叭/肢体/屏幕等。 每个终端保持一个 priority queue，每个终端的task任务必须串行。
 */
public class Brain implements SoundTranslator.OnTranslatorListener {
    private ILimbs limbs;
    private SoundTranslator soundTranslator;

    public Brain(ILimbs limbs) {
        this.limbs = limbs;

        soundTranslator = new SoundTranslator();
        soundTranslator.setTranslatorListener(this);

        LimbTaskQueue.setLimbs(this.limbs);
    }

    public void translateSound(String s) {
        soundTranslator.addTask(s);
    }

    public void addCommand(Command command) {
        LogUtils.d(Constant.TAG_COMMAND, "add command:" + command.toString());
        switch (command.getType()) {
            case PLAY_SOUND:
                //讲话
                MouthTaskQueue.addTask(Priority.DEFAULT, command);
                break;
            case MECHANICAL_MOVEMENT:
                //肢体运动
                LimbTaskQueue.addTask(Priority.DEFAULT, command.getContent());
                break;
            case SHOW_EXPRESSION:
                //面部表情
                FaceManager.display(command.getContent());
                break;
            case PLAY_MUSIC:
                MouthTaskQueue.addTask(Priority.DEFAULT, command);
                break;
        }
    }

    public void addCommand(List<Command> commands) {
        if (commands == null || commands.isEmpty()) return;
        for (Command command : commands) {
            addCommand(command);
        }
    }

    @Override
    public void onTranslateComplete(List<Command> commands) {
        addCommand(commands);
    }
}
