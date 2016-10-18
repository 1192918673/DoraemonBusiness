package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BL.BLM;
import com.geeknewbee.doraemon.entity.event.LimbActionCompleteEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.SyncQueueEmptyEvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.output.action.AISpeechTTS;
import com.geeknewbee.doraemon.output.action.ITTS;
import com.geeknewbee.doraemon.output.action.LimbsManager;
import com.geeknewbee.doraemon.output.action.MediaPlayerHelper;
import com.geeknewbee.doraemon.processcenter.command.BLCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.LocalResourceCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.processcenter.command.SyncCommand;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SyncQueue extends AbstractTaskQueue<SyncCommand, Boolean> {
    public static final String TAG = SyncQueue.class.getSimpleName();
    public static volatile SyncQueue instance;
    private BlockingQueue<SyncCommand> soundCommands;
    private ITTS itts;
    private MediaPlayerHelper mediaPlayerHelper;


    private SyncCommand activeCommand;

    private SyncQueue() {
        soundCommands = new ArrayBlockingQueue<SyncCommand>(50);
        itts = new AISpeechTTS();

        mediaPlayerHelper = new MediaPlayerHelper();
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

    @Override
    public Boolean performTask(SyncCommand syncCommand) {
        LogUtils.d(TAG, "performTask");

        try {
            if (syncCommand.delayTime > 0)
                Thread.sleep(syncCommand.delayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Command command : syncCommand.commandList) {
            switch (command.getType()) {
                case PLAY_SOUND:
                    itts.addSoundCommand((SoundCommand) command);
                    break;
                case PLAY_LOCAL_RESOURCE:
                    LocalResourceCommand resourceCommand = (LocalResourceCommand) command;
                    mediaPlayerHelper.start(App.mContext, resourceCommand);
                    break;
                case SPORT_ACTION_SET:
                    LimbsManager.getInstance().addTask(command);
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
        return true;
    }

    @Override
    public void onTaskComplete(Boolean output) {

    }

    private void scheduleNext() {
        if ((activeCommand = soundCommands.poll()) != null) {
            LogUtils.d(TAG, "scheduleNext:" + true);
            addTask(activeCommand); //后台执行
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
        activeCommand = null;
        soundCommands.clear();
        LogUtils.d(TAG, "stop");
        LimbsManager.getInstance().stop();
        mediaPlayerHelper.stop();
        itts.stop();
        clearTasks();
    }
}
