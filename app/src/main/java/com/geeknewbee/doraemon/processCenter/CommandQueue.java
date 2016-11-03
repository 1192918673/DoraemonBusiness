package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.entity.event.CommandCompleteEvent;
import com.geeknewbee.doraemon.entity.event.FaceControlCompleteEvent;
import com.geeknewbee.doraemon.entity.event.LimbActionCompleteEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.entity.event.VideoCompleteEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.command.Command;
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

public class CommandQueue {
    public static final String TAG = CommandQueue.class.getSimpleName();
    public static volatile CommandQueue instance;
    private final Context context;
    //等待执行的SyncCommand 集合
    private final TreeSet<SyncCommand> soundCommands;
    private ExecutorService executorService;
    //正在执行的SyncCommand Queue
    private List<SyncCommand> activeCommandList;
    private final Handler mHandler;

    private CommandQueue(Context context) {
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

    public static CommandQueue getInstance(Context context) {
        if (instance == null) {
            synchronized (CommandQueue.class) {
                if (instance == null) {
                    instance = new CommandQueue(context);
                }
            }
        }
        return instance;
    }

    public void addCommand(final SyncCommand command) {
        synchronized (soundCommands) {
            if (command == null || command.commandList == null || command.commandList.isEmpty())
                return;

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
                    command.getType().getOutput().addCommand(command);
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
     * 当人脸操作完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onFaceControlComplete(FaceControlCompleteEvent event) {
        markAndTryDoNextCommand(event.getId(), null);
    }

    /**
     * 当其他命令操作完成(设置表情，系统设置,智能家电等)
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onOtherCommandComplete(CommandCompleteEvent event) {
        markAndTryDoNextCommand(event.getId(), null);
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
        synchronized (soundCommands) {
            soundCommands.clear();
        }
        MouthTaskQueue.getInstance().stop();
        LimbsTaskQueue.getInstance().stop();
    }

    public void interrupt() {
        LogUtils.d(TAG, "interrupt");
        MouthTaskQueue.getInstance().interrupt();
        LimbsTaskQueue.getInstance().interrupt();
    }
}