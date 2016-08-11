package com.geeknewbee.doraemonsdk.processcenter;

import com.geeknewbee.doraemonsdk.constants.Constants;
import com.geeknewbee.doraemonsdk.output.FaceManager;
import com.geeknewbee.doraemonsdk.output.SysSettingManager;
import com.geeknewbee.doraemonsdk.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemonsdk.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemonsdk.processcenter.command.Command;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.List;

/**
 * 任务处理中枢
 * 对声音、人脸输入等 进行各种响应
 * 输出行为有 语音| 四肢运动|显示表情|播放电影等
 * 输出终端有 喇叭/肢体/屏幕等。 每个终端保持一个 priority queue，每个终端的task任务必须串行。
 */
public class Brain implements ISoundTranslate.OnTranslatorListener {
    private ISoundTranslate soundTranslate;

    public void translateSound(String s) {
        soundTranslate.translateSound(s);
    }

    public void addCommand(Command command) {
        LogUtils.d(Constants.TAG_COMMAND, "add command:" + command.toString());
        switch (command.getType()) {
            case PLAY_SOUND:
                //讲话
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case MECHANICAL_MOVEMENT:
                //肢体运动
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case SHOW_EXPRESSION:
                //面部表情
                FaceManager.display(command.getContent());
                break;
            case PLAY_MUSIC:
                // 音乐
                MouthTaskQueue.getInstance().addTask(command);
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
            case DANCE:
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case LE_XING_FOOT://乐行Foot
                LimbsTaskQueue.getInstance().addTask(command);
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

    public void setSoundTranslate(ISoundTranslate soundTranslate) {
        this.soundTranslate = soundTranslate;
        this.soundTranslate.setTranslatorListener(this);
    }
}