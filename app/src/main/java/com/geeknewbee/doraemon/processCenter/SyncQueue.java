package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.entity.event.LimbActionCompleteEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.entity.event.VideoCompleteEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.BLM;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.output.OtherCommandManager;
import com.geeknewbee.doraemon.output.ReadFaceManager;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.processcenter.command.SyncCommand;
import com.geeknewbee.doraemonsdk.task.Priority;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncQueue {
    public static final String TAG = SyncQueue.class.getSimpleName();
    public static volatile SyncQueue instance;
    private final Context context;
    //等待执行的SyncCommand 集合
    private final TreeSet<SyncCommand> soundCommands;
    private ExecutorService executorService;
    //正在执行的SyncCommand Queue
    private List<SyncCommand> activeCommandList;
    private final Handler mHandler;

    private SyncQueue(Context context) {
        this.context = context;
        soundCommands = new TreeSet<>();
        activeCommandList = new ArrayList<>();
        executorService = Executors.newSingleThreadExecutor();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                tryPerformCommand(null);
            }
        };

        EventBus.getDefault().register(this);
    }

    public static SyncQueue getInstance(Context context) {
        if (instance == null) {
            synchronized (SyncQueue.class) {
                if (instance == null) {
                    instance = new SyncQueue(context);
                }
            }
        }
        return instance;
    }

    public void addCommand(final SyncCommand command) {
        synchronized (soundCommands) {
            soundCommands.add(command);
            LogUtils.d(TAG, "add SyncCommand:" + command + " soundCommands size:" + soundCommands.size());
            if (command.getDelayTime() > 0) {
                //为delay command开启定时
                LogUtils.d(TAG, "is delay command :" + command.getDelayTime());
                mHandler.sendEmptyMessageDelayed(0, command.getDelayTime());
            }

            tryPerformCommand(null);
        }
    }

    /**
     * 去执行命令集合中的命令
     *
     * @param soundMonitorType 如果当前命令集合中没有命令可以执行，需要切换的模式
     */
    private void tryPerformCommand(SoundMonitorType soundMonitorType) {
        synchronized (soundCommands) {
            LogUtils.d(TAG, "tryPerformCommand: size:" + soundCommands.size() + " thread:" + Thread.currentThread());
            if (soundCommands.size() < 1) {
                //一直等到最后一个任务执行完成再进行，声音模式的切换。不用每次执行完成都进行切换
                if (soundMonitorType != null)
                    Doraemon.getInstance(App.mContext).switchSoundMonitor(soundMonitorType);
                return;
            }

            Iterator<SyncCommand> iter = soundCommands.iterator();
            while (iter.hasNext()) {
                SyncCommand next = iter.next();
                if (canPerform(next)) {
                    iter.remove();
                    performCommand(next);
                } else if (System.currentTimeMillis() > next.getExpireTimestamp()) {
                    //过期的Command的直接删除
                    if (!iter.hasNext()) {
                        //如果是最后的一个，需要切换到指定的模式
                        LogUtils.d(TAG, "is last command and is expire");
                        if (soundMonitorType != null)
                            Doraemon.getInstance(App.mContext).switchSoundMonitor(soundMonitorType);
                    }
                    iter.remove();
                    LogUtils.d(TAG, "delete expire syncCommand" + next);
                } else
                    LogUtils.d(TAG, "can not perform" + next);
            }
        }
    }

    /**
     * 判断syncCommand 是否可以执行(对应的所有的输出通道是否都可用)
     *
     * @param command
     * @return
     */
    private boolean canPerform(SyncCommand command) {
        if (allOutputCanUse(command)) {
            long now = System.currentTimeMillis();
            //判断是否在到了执行时间，并且没有过期
            return (now >= command.getStartTimestamp() && now < command.getExpireTimestamp());
        } else
            return false;
    }

    /**
     * 对应的所有的输出通道是否都可用
     *
     * @param command
     * @return
     */
    private boolean allOutputCanUse(SyncCommand command) {
        //1.如果优先级是中断 则 可以执行
        if (command.getPriority() == Priority.INTERRUPT) {
            return true;
        }

        //2.判断对应的所有的输出通道是否可用
        for (Command command1 : command.getCommandList()) {
            if (command1.getType().getOutput().isBusy())
                return false;
        }
        return true;
    }

    private void performCommand(final SyncCommand syncCommand) {
        LogUtils.d(TAG, "performCommand:" + syncCommand);
        //执行之前先占用输出通道
        for (Command command : syncCommand.commandList) {
            LogUtils.d(TAG, "11111111------------- command type:" + command.getType());
            command.getType().getOutput().setBusy(true);
        }

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                activeCommandList.add(syncCommand);
                //1.如果是中断的，需要先中断当前的任务
                if (syncCommand.getPriority() == Priority.INTERRUPT) {
                    LogUtils.d(TAG, "3333333333333-----------interrupt");
                    interrupt();
                }
                //2.如果需要启动EDD，启动EDD模式
                if (syncCommand.needSwitchEdd)
                    Doraemon.getInstance(context).switchSoundMonitor(SoundMonitorType.EDD);

                for (Command command : syncCommand.commandList) {
                    LogUtils.d(TAG, "22222222222-----------command type:" + command.getType());

                    switch (command.getType()) {
                        case PLAY_SOUND:
                        case PLAY_LOCAL_RESOURCE:
                        case PLAY_MUSIC: //音乐
                        case PLAY_JOKE: //笑话
                        case PLAY_MOVIE:
                        case LEARN_EN:  //学英语
                            LogUtils.d(TAG, "add Mouth task");
                            MouthTaskQueue.getInstance().addCommand(command);
                            break;
                        case SPORT_ACTION_SET:
                        case BLUETOOTH_CONTROL_FOOT: //蓝牙控制脚步
                            LimbsTaskQueue.getInstance().addCommand(command);
                            break;
                        case PERSON_START:
                        case PERSON_ADD_FACE:
                        case PERSON_SET_NAME:
                        case PERSON_DELETE_ALL:
                            ReadFaceManager.getInstance(App.mContext).addCommand(command);
                            break;
                        case SHOW_EXPRESSION:
                            ExpressionCommand expressionCommand = (ExpressionCommand) command;
                            FaceManager.getInstance().addCommand(expressionCommand);
                            markAndTryDoNextCommand(expressionCommand.getId(), null);//表情的命令执行时调用命令后就认为成功了
                            break;
                        case BL://博联遥控
                        case BL_SP: //博联插座
                            BLM.getInstance().addCommand(command);
                            markAndTryDoNextCommand(command.getId(), null);//命令执行时调用命令后就认为成功了
                            break;
                        case STOP:
                        case TAKE_PICTURE: //拍照
//                            addCommand(new SoundCommand("好的", SoundCommand.InputSource.TIPS));
                        case SETTING_WIFI://设置连接WIFI
                        case SETTING_VOLUME://设置系统音量
                        case SLEEP:
                            OtherCommandManager.getInstance().addCommand(command);
                            markAndTryDoNextCommand(command.getId(), null);//命令执行时调用命令后就认为成功了
                            break;
                    }
                }
            }
        });
    }

    /**
     * TTS 语音合成完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onTTSComplete(TTSCompleteEvent event) {
        SoundMonitorType monitorType = null;
        if (event.inputSource == SoundCommand.InputSource.TIPS) {
            //提醒类文本不改变原有状态
        } else if (event.inputSource == SoundCommand.InputSource.START_WAKE_UP) {
            //现在唤醒是在提示唤醒词后才开启唤醒
            monitorType = SoundMonitorType.EDD;
        } else if (event.inputSource == SoundCommand.InputSource.SOUND_TRANSLATE ||
                event.inputSource == SoundCommand.InputSource.AFTER_WAKE_UP)
            monitorType = SoundMonitorType.ASR;

        markAndTryDoNextCommand(event.getTTSCommandID(), monitorType);
    }

    /**
     * 当音乐播放完成(包括 音乐，笑话)
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPlayMusicComplete(MusicCompleteEvent event) {
        markAndTryDoNextCommand(event.commandId, SoundMonitorType.ASR);
    }

    /**
     * 肢体运动完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onLimbActionComplete(LimbActionCompleteEvent event) {
        switch (event.inputSource) {
            case INTERNAL:
                markAndTryDoNextCommand(event.commandId, SoundMonitorType.ASR);
                break;
            case REMOTE_CONTROL: //远程控制不需要切换模式
                markAndTryDoNextCommand(event.commandId, null);
                break;
        }
    }

    /**
     * 当视频播放完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onVideoPlayComplete(VideoCompleteEvent event) {
        markAndTryDoNextCommand(event.commandId, SoundMonitorType.ASR);
    }

    /**
     * 标记完当前的任务，并判断当前的一组的任务集是否都已经完成，如果完成就去执行下一组命令集
     *
     * @param commandID
     * @param monitorType
     */
    private void markAndTryDoNextCommand(String commandID, SoundMonitorType monitorType) {
        if (activeCommandList != null && activeCommandList.size() > 0) {
//            LogUtils.d(TAG, "complete command id:" + commandID);
            for (SyncCommand syncCommand : activeCommandList) {
                if (syncCommand.contains(commandID)) {
                    syncCommand.executeComplete(commandID);
                    if (syncCommand.isComplete()) {
                        //执行完成后释放先占用输出通道
                        for (Command command : syncCommand.commandList) {
                            command.getType().getOutput().setBusy(false);
                        }
                        activeCommandList.remove(syncCommand);
                        LogUtils.d(TAG, "syncCommand complete " + syncCommand.toString() + " activeCommandList size:" + activeCommandList.size());
                        tryPerformCommand(monitorType);
                    } else
                        LogUtils.d(TAG, "activeCommandList not complete" + " activeCommandList size:" + activeCommandList.size());
                    break;
                }
            }
        } else
            LogUtils.d(TAG, "activeCommandList is null");
    }

    public void stop() {
        LogUtils.d(TAG, "stop");
        MouthTaskQueue.getInstance().stop();
        LimbsTaskQueue.getInstance().stop();
        MouthTaskQueue.getInstance().setBusy(false);
        LimbsTaskQueue.getInstance().setBusy(false);
    }

    public void interrupt() {
        LogUtils.d(TAG, "interrupt");
        MouthTaskQueue.getInstance().interrupt();
        LimbsTaskQueue.getInstance().interrupt();
    }
}
