package com.geeknewbee.doraemon.control;

import com.geeknewbee.doraemon.task.FaceManager;
import com.geeknewbee.doraemon.task.LimbTaskQueue;
import com.geeknewbee.doraemon.task.MouthTaskQueue;
import com.geeknewbee.doraemon.task.SoundTranslateTaskQueue;
import com.geeknewbee.doraemon.task.SysSettingManager;
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
public class Brain implements SoundTranslateTaskQueue.OnTranslatorListener {

    public Brain() {
        SoundTranslateTaskQueue.getInstance().setTranslatorListener(this);
    }

    public void translateSound(String s) {
        SoundTranslateTaskQueue.getInstance().addTask(Priority.DEFAULT, s);
    }

    public void addCommand(Command command) {
        LogUtils.d(Constant.TAG_COMMAND, "add command:" + command.toString());
        switch (command.getType()) {
            case PLAY_SOUND:
                //讲话
                MouthTaskQueue.getInstance().addTask(Priority.DEFAULT, command);
                break;
            case MECHANICAL_MOVEMENT:
                //肢体运动
                LimbTaskQueue.getInstance().addTask(Priority.DEFAULT, command.getContent());
                break;
            case SHOW_EXPRESSION:
                //面部表情
                FaceManager.display(command.getContent());
                break;
            case PLAY_MUSIC:
                // 音乐
                MouthTaskQueue.getInstance().addTask(Priority.DEFAULT, command);
                break;
            case STOP:
                MouthTaskQueue.getInstance().stop();
                break;
            case WIFI_MESSAGE:// 设置连接WIFI
                SysSettingManager.connectWiFi(command.getContent());
                break;
            case SETTING_VOLUME:// 设置系统音量
                SysSettingManager.setVolume(command.getContent());
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
