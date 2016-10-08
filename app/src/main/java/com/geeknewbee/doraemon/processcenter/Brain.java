package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BL.BLM;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.output.ReadFace;
import com.geeknewbee.doraemon.output.SysSettingManager;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.command.BLCommand;
import com.geeknewbee.doraemon.processcenter.command.BLSPCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.WifiCommand;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 任务处理中枢
 * 对声音、人脸输入等 进行各种响应
 * 输出行为有 语音| 四肢运动|显示表情|播放电影等
 * 输出终端有 喇叭/肢体/屏幕等。 每个终端保持一个 priority queue，每个终端的task任务必须串行。
 */
public class Brain implements SoundTranslateTaskQueue.OnTranslatorListener {
    //创建一个切换AddCommand锁对象
    private Lock addCommandLock = new ReentrantLock();


    public void translateSound(SoundTranslateInput input) {
        LogUtils.d(AISpeechEar.TAG, "translateSound");
        SoundTranslateTaskQueue.getInstance().setTranslatorListener(this);
        SoundTranslateTaskQueue.getInstance().addTask(input);
    }

    public void addCommand(Command command) {
        addCommandLock.lock();
        LogUtils.d(Constants.TAG_COMMAND, "add command:" + command.toString());
        switch (command.getType()) {
            case SHOW_EXPRESSION: //面部表情
                ExpressionCommand expressionCommand = (ExpressionCommand) command;
                FaceManager.getInstance().displayGif(expressionCommand.getContent(), expressionCommand.loops);
                break;
            case PLAY_SOUND: //讲话
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case PLAY_MUSIC: //音乐
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case PLAY_JOKE: //笑话
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case PLAY_LOCAL_RESOURCE: //播放本地音频
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case MECHANICAL_MOVEMENT: //肢体运动
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case LE_XING_FOOT://乐行Foot
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case BLUETOOTH_CONTROL_FOOT: //蓝牙控制脚步
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case ACTIONSET: //舞蹈动作
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case STOP:
                MouthTaskQueue.getInstance().stop();
                LimbsTaskQueue.getInstance().stop();
                break;
            case TAKE_PICTURE: //拍照
//                addCommand(new SoundCommand("好的《3》《2》1。。。拍好了", SoundCommand.InputSource.TIPS));
                Doraemon.getInstance(BaseApplication.mContext).startTakePicture();
                break;
            case WIFI_MESSAGE://设置连接WIFI
                WifiCommand wifiCommand = (WifiCommand) command;
                SysSettingManager.connectWiFi(wifiCommand.ssid, wifiCommand.pwd, wifiCommand.type);
                break;
            case SETTING_VOLUME://设置系统音量
                SysSettingManager.setVolume(command.getContent());
                break;
            case BL: //博联遥控
                BLCommand blCommand = (BLCommand) command;
                BLM.broadLinkRMProSend(blCommand.getResponse());
                break;
            case BL_SP: //博联插座
                BLSPCommand blspCommand = (BLSPCommand) command;
                BLM.modifyPlugbase(blspCommand.getInput(), blspCommand.getMac().trim());
                break;
            case PLAY_MOVIE:
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case DANCE:
                LimbsTaskQueue.getInstance().addTask(command);
                break;
            case SLEEP:
                EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                break;
            case LEARN_EN:  //学英语
                MouthTaskQueue.getInstance().addTask(command);
                break;
            case PERSON_START:
            case PERSON_ADD_FACE:
            case PERSON_SET_NAME:
            case PERSON_DELETE_ALL:
                ReadFace.getInstance(App.mContext).addCommand(command);
                break;
        }
        addCommandLock.unlock();
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
