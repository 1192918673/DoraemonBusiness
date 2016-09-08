package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.input.ReadSenseEye;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.output.SysSettingManager;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.WifiCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.List;

/**
 * 任务处理中枢
 * 对声音、人脸输入等 进行各种响应
 * 输出行为有 语音| 四肢运动|显示表情|播放电影等
 * 输出终端有 喇叭/肢体/屏幕等。 每个终端保持一个 priority queue，每个终端的task任务必须串行。
 */
public class Brain implements SoundTranslateTaskQueue.OnTranslatorListener {

    public void translateSound(SoundTranslateInput input) {
        LogUtils.d(AISpeechEar.TAG, "translateSound");
        SoundTranslateTaskQueue.getInstance().setTranslatorListener(this);
        SoundTranslateTaskQueue.getInstance().addTask(input);
    }

    public void addCommand(Command command) {
        LogUtils.d(Constants.TAG_COMMAND, "add command:" + command.toString());
//        Doraemon.getInstance(App.mContext).stopASR();
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
                ExpressionCommand expressionCommand = (ExpressionCommand) command;
                FaceManager.getInstance().displayGif(expressionCommand.getContent(), expressionCommand.loops);
                break;
            case READ_SENCE:
                //拍照、人脸添加、人脸识别
                ReadSenseEye.getInstance().startTakePicture(false);
                break;
            case PLAY_MUSIC:
                // 音乐
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case PLAY_JOKE:
                // 笑话
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case STOP:
                MouthTaskQueue.getInstance().stop();
                LimbsTaskQueue.getInstance().stop();
                break;
            case WIFI_MESSAGE:// 设置连接WIFI
                WifiCommand wifiCommand = (WifiCommand) command;
                SysSettingManager.connectWiFi(wifiCommand.ssid, wifiCommand.pwd, wifiCommand.type);
                break;
            case SETTING_VOLUME:// 设置系统音量
                SysSettingManager.setVolume(command.getContent());
                break;
            case ACTIONSET:
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case LE_XING_FOOT://乐行Foot
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case SHOW_QR:
                FaceManager.getInstance().showQR(command.getContent());
                break;
            case BIND_ACCOUNT_SUCCESS:
                FaceManager.getInstance().hideQR();
                break;
            case PLAY_LOCAL_RESOURCE:
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case BLUETOOTH_CONTROL_FOOT:
                LimbsTaskQueue.getInstance().addTask(command);
                break;
        }
    }

    protected void addCommand(List<Command> commands) {
        if (commands == null || commands.isEmpty()) return;
        for (Command command : commands) {
            addCommand(command);
        }
    }

    @Override
    public void onTranslateComplete(List<Command> commands) {
        LogUtils.d(AISpeechEar.TAG, "onTranslateComplete");
        addCommand(commands);
        EventManager.sendTranslateSoundComplete();
    }
}
