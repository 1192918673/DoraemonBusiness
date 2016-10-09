package com.geeknewbee.doraemon.output.queue;

import android.content.Intent;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.entity.event.VideoPlayCreate;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.action.AISpeechTTS;
import com.geeknewbee.doraemon.output.action.IMusicPlayer;
import com.geeknewbee.doraemon.output.action.ITTS;
import com.geeknewbee.doraemon.output.action.IVideoPlayer;
import com.geeknewbee.doraemon.output.action.MediaPlayerHelper;
import com.geeknewbee.doraemon.output.action.XMLYMusicPlayer;
import com.geeknewbee.doraemon.output.action.XfSpeechTTS;
import com.geeknewbee.doraemon.output.action.YouKuPlayerActivity;
import com.geeknewbee.doraemon.processcenter.LearnEnglish;
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
public class MouthTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    private volatile static MouthTaskQueue instance;
    private ITTS itts;
    private ITTS ittXF;
    private IMusicPlayer iMusicPlayer;
    private MediaPlayerHelper mediaPlayerHelper;
    private IVideoPlayer videoPlayer;
    private LearnEnglish learnEnglish;

    private MouthTaskQueue() {
        super();
        itts = new AISpeechTTS();
        ittXF = new XfSpeechTTS();
        iMusicPlayer = new XMLYMusicPlayer();
        mediaPlayerHelper = new MediaPlayerHelper();
        learnEnglish = new LearnEnglish();
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
                if (soundCommand.inputSource != SoundCommand.InputSource.START_WAKE_UP
                        && soundCommand.inputSource != SoundCommand.InputSource.AFTER_WAKE_UP)
                    EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));

                if (soundCommand.inputSource == SoundCommand.InputSource.IOS_BUSINESS)
                    ittXF.addSoundCommand(soundCommand, true);//商业版的需要覆盖正在执行的任务
                else
                    itts.addSoundCommand(soundCommand, soundCommand.isOverwrite);
                break;
            case PLAY_MUSIC:
                EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                itts.talk("正在搜索音乐", SoundCommand.InputSource.TIPS);
                iMusicPlayer.play(input.getContent());
                break;
            case PLAY_JOKE:
                EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                iMusicPlayer.joke();
                break;
            case PLAY_LOCAL_RESOURCE:
                EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                LocalResourceCommand resourceCommand = (LocalResourceCommand) input;
                mediaPlayerHelper.start(App.mContext, resourceCommand.resourceID);
                break;
            case PLAY_MOVIE:
                EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));

                Intent intent = new Intent(App.mContext, YouKuPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(YouKuPlayerActivity.EXTRA_VID, input.getContent());
                App.mContext.startActivity(intent);
                break;
            case LEARN_EN:  //学英语
                EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.CLOSE_ALL));
                learnEnglish.init();
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
        learnEnglish.stop();
        itts.stop();
        ittXF.stop();
        iMusicPlayer.stop();
        mediaPlayerHelper.stop();
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer = null;
        }
        clearTasks();
    }

    public synchronized boolean isBusy() {
        return itts.isSpeaking()
                || ittXF.isSpeaking()
                || iMusicPlayer.isPlaying()
                || mediaPlayerHelper.isPlaying()
                || (videoPlayer != null && videoPlayer.isPlaying()
                || learnEnglish.isLearnning());
    }

    public void destroy() {
        learnEnglish.destory();
        itts.destroy();
        ittXF.destroy();
        iMusicPlayer.destroy();
        mediaPlayerHelper.destroy();
        if (videoPlayer != null) {
            videoPlayer.stop();
        }
    }
}
