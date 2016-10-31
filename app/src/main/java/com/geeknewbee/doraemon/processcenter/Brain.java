package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.entity.event.TranslateSoundCompleteEvent;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.processcenter.command.SyncCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
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
        LogUtils.d(SyncQueue.TAG, "add command:" + command.getType() + "-" + command.getId());

        SyncCommand syncCommand = new SyncCommand(Collections.singletonList(command));
        if (command.getType() == CommandType.PLAY_SOUND) {
            SoundCommand soundCommand = (SoundCommand) command;
            if (soundCommand.inputSource == SoundCommand.InputSource.START_WAKE_UP
                    || soundCommand.inputSource == SoundCommand.InputSource.AFTER_WAKE_UP) {
                //提示唤醒词、唤醒之后的语音命令执行前不需要进入EDD
                syncCommand.needSwitchEdd = false;
            }
        } else if (command.getType() == CommandType.SHOW_EXPRESSION ||
                command.getType() == CommandType.BLUETOOTH_CONTROL_FOOT ||
                command.getType() == CommandType.STOP)
            //单独的设置Gif、STOP、蓝牙控制脚步执行前不需要进入EDD
            syncCommand.needSwitchEdd = false;

        SyncQueue.getInstance(App.mContext).addCommand(syncCommand);
        LogUtils.d(Constants.TAG_COMMAND, "add command:" + command.toString());
    }

    protected void addCommand(List<Command> commands) {
        String logStr = "addCommand list:";
        for (Command command : commands) {
            logStr += (command.getType().toString() + "-" + command.getId());
        }
        LogUtils.d(SyncQueue.TAG, logStr);
        SyncCommand syncCommand = new SyncCommand(commands);
        SyncQueue.getInstance(App.mContext).addCommand(syncCommand);
    }

    @Override
    public void onTranslateComplete(List<Command> commands) {
        LogUtils.d(AISpeechEar.TAG, "onTranslateComplete");
        addCommand(commands);
        EventBus.getDefault().post(new TranslateSoundCompleteEvent());
    }

    public void addCommand(SyncCommand syncCommand) {
        SyncQueue.getInstance(App.mContext).addCommand(syncCommand);
    }
}
