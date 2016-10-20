package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.BL.BLM;
import com.geeknewbee.doraemon.entity.event.LimbActionCompleteEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.SyncQueueEmptyEvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.command.BLCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.SyncCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncQueue {
    public static final String TAG = SyncQueue.class.getSimpleName();
    public static volatile SyncQueue instance;
    private final ExecutorService executorService;
    private BlockingQueue<SyncCommand> soundCommands;
    private SyncCommand activeCommand;

    private SyncQueue() {
        soundCommands = new ArrayBlockingQueue<SyncCommand>(50);
        executorService = Executors.newSingleThreadExecutor();
        EventBus.getDefault().register(this);
    }

    public static SyncQueue getInstance() {
        if (instance == null) {
            synchronized (SyncQueue.class) {
                if (instance == null) {
                    instance = new SyncQueue();
                }
            }
        }
        return instance;
    }

    public synchronized void addCommand(SyncCommand command) {
        LogUtils.d(TAG, "add command");
        soundCommands.offer(command);
        if (activeCommand == null)
            scheduleNext();
        else
            LogUtils.d(TAG, "activeCommand is not null");
    }

    public void performCommand(final SyncCommand syncCommand) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "performCommand");
                try {
                    if (syncCommand.delayTime > 0)
                        Thread.sleep(syncCommand.delayTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (Command command : syncCommand.commandList) {
                    switch (command.getType()) {
                        case PLAY_SOUND:
                        case PLAY_LOCAL_RESOURCE:
                            MouthTaskQueue.getInstance().addTask(command);
                            break;
                        case SPORT_ACTION_SET:
                            LimbsTaskQueue.getInstance().addTask(command);
                            break;
                        case SHOW_EXPRESSION:
                            ExpressionCommand expressionCommand = (ExpressionCommand) command;
                            FaceManager.getInstance().displayGif(expressionCommand.getContent(), expressionCommand.loops);
                            markAndTryDoNextCommand(expressionCommand.getId());//表情的命令执行时调用命令后就认为成功了
                            break;
                        case BL:
                            BLCommand blCommand = (BLCommand) command;
                            BLM.broadLinkRMProSend(blCommand.getResponse());
                            markAndTryDoNextCommand(blCommand.getId());//智能家居的命令执行时调用命令后就认为成功了
                            break;
                    }
                }
            }
        });
    }

    private void scheduleNext() {
        if ((activeCommand = soundCommands.poll()) != null) {
            LogUtils.d(TAG, "scheduleNext:" + true);
            performCommand(activeCommand); //后台执行
        } else {
            LogUtils.d(TAG, "scheduleNext:" + false);
            EventBus.getDefault().post(new SyncQueueEmptyEvent());
        }
    }

    /**
     * TTS 语音合成完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTTSComplete(TTSCompleteEvent event) {
        markAndTryDoNextCommand(event.getTTSCommandID());
    }

    /**
     * 当音乐播放完成(包括 音乐，笑话)
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayMusicComplete(MusicCompleteEvent event) {
        markAndTryDoNextCommand(event.commandId);
    }

    /**
     * 肢体运动完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLimbActionComplete(LimbActionCompleteEvent event) {
        markAndTryDoNextCommand(event.commandId);
    }

    /**
     * 标记完当前的任务，并判断当前的一组的任务集是否都已经完成，如果完成就去执行下一组命令集
     *
     * @param commandID
     */
    private synchronized void markAndTryDoNextCommand(long commandID) {
        if (activeCommand != null) {
            LogUtils.d(TAG, "complete command id:" + commandID);
            activeCommand.executeComplete(commandID);
            if (activeCommand.isComplete())
                scheduleNext();
            else
                LogUtils.d(TAG, "activeCommand not complete");
        } else
            LogUtils.d(TAG, "activeCommand is null");
    }

    public void stop() {
        LogUtils.d(TAG, "stop");
        activeCommand = null;
        soundCommands.clear();
        MouthTaskQueue.getInstance().stop();
        LimbsTaskQueue.getInstance().stop();
    }
}
