package com.geeknewbee.doraemon.output.queue;

import android.content.Intent;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.entity.event.VideoPlayCreate;
import com.geeknewbee.doraemon.output.IOutput;
import com.geeknewbee.doraemon.output.action.AISpeechTTS;
import com.geeknewbee.doraemon.output.action.IMusicPlayer;
import com.geeknewbee.doraemon.output.action.ITTS;
import com.geeknewbee.doraemon.output.action.IVideoPlayer;
import com.geeknewbee.doraemon.output.action.MediaPlayerHelper;
import com.geeknewbee.doraemon.output.action.XMLYMusicPlayer;
import com.geeknewbee.doraemon.output.action.XfSpeechTTS;
import com.geeknewbee.doraemon.output.action.YouKuPlayerActivity;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.LocalResourceCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 声音 task queue
 */
public class MouthTaskQueue extends AbstractTaskQueue<Command, Boolean> implements IOutput {
    private volatile static MouthTaskQueue instance;
    private ITTS itts;
    private ITTS ittXF;
    private IMusicPlayer iMusicPlayer;
    private MediaPlayerHelper mediaPlayerHelper;
    private IVideoPlayer videoPlayer;
    private boolean isBusy;

    private MouthTaskQueue() {
        super();
        itts = AISpeechTTS.getInstance();
        ittXF = new XfSpeechTTS();
        iMusicPlayer = new XMLYMusicPlayer();
        mediaPlayerHelper = new MediaPlayerHelper();
        EventBus.getDefault().register(this);
    }

    public static MouthTaskQueue getInstance() {
        if (instance == null) {
            synchronized (MouthTaskQueue.class) {
                if (instance == null) {
                    instance = new MouthTaskQueue();
                }
            }
        }
        return instance;
    }

    public void reTTS() {
        itts.reInit();
        ittXF.reInit();
    }

    public void reMusicPlayer() {
        iMusicPlayer.reInit();
    }


    @Override
    public Boolean performTask(Command input) {
        switch (input.getType()) {
            case PLAY_SOUND:
                SoundCommand soundCommand = (SoundCommand) input;

                if (soundCommand.inputSource == SoundCommand.InputSource.IOS_BUSINESS)
                    ittXF.addSoundCommand(soundCommand);//商业版的需要覆盖正在执行的任务
                else
                    itts.addSoundCommand(soundCommand);
                break;
            case PLAY_MUSIC:
                iMusicPlayer.play(input);
                break;
            case PLAY_JOKE:
                iMusicPlayer.joke(input);
                break;
            case PLAY_LOCAL_RESOURCE:
                LocalResourceCommand resourceCommand = (LocalResourceCommand) input;
                mediaPlayerHelper.start(App.mContext, resourceCommand);
                break;
            case PLAY_MOVIE:
                Intent intent = new Intent(App.mContext, YouKuPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(YouKuPlayerActivity.EXTRA_VID, input.getContent());
                intent.putExtra(YouKuPlayerActivity.EXTRA_COMMAND_ID, input.getId());
                App.mContext.startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onTaskComplete(Boolean output) {

    }

    /**
     * 当 视频播放被创建的时候
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoPlayerEvent(VideoPlayCreate event) {
        videoPlayer = event.videoPlayer;
    }

    public void stop() {
        stopCurrentTask();
        clearTasks();
    }

    private void stopCurrentTask() {
        itts.stop();
        ittXF.stop();
        iMusicPlayer.stop();
        mediaPlayerHelper.stop();
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer = null;
        }
    }

    @Override
    public synchronized boolean isBusy() {
        return isBusy;
    }

    @Override
    public void setBusy(boolean isBusy) {
        this.isBusy = isBusy;
    }

    public void destroy() {
        itts.destroy();
        ittXF.destroy();
        iMusicPlayer.destroy();
        mediaPlayerHelper.destroy();
        if (videoPlayer != null) {
            videoPlayer.stop();
        }
    }

    @Override
    public void addCommand(Command command) {
        addTask(command);
    }

    public void interrupt() {
        stopCurrentTask();
    }
}
