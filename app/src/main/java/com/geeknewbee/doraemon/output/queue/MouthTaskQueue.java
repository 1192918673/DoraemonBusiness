package com.geeknewbee.doraemon.output.queue;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.action.AISpeechTTS;
import com.geeknewbee.doraemon.output.action.IMusicPlayer;
import com.geeknewbee.doraemon.output.action.ITTS;
import com.geeknewbee.doraemon.output.action.MediaPlayerHelper;
import com.geeknewbee.doraemon.output.action.XMLYMusicPlayer;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.LocalResourceCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;

import org.greenrobot.eventbus.EventBus;

/**
 * 声音 task queue
 */
public class MouthTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    private volatile static MouthTaskQueue instance;
    private ITTS itts;
    private IMusicPlayer iMusicPlayer;
    private MediaPlayerHelper mediaPlayerHelper;

    private MouthTaskQueue() {
        super();
        itts = new AISpeechTTS();
        iMusicPlayer = new XMLYMusicPlayer();
        mediaPlayerHelper = new MediaPlayerHelper();
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

    public void reInit() {
        itts.reInit();
    }

    @Override
    public Boolean performTask(Command input) {
        switch (input.getType()) {
            case PLAY_SOUND:
                SoundCommand soundCommand = (SoundCommand) input;
                if (soundCommand.inputSource != SoundCommand.InputSource.START_WAKE_UP)
                    EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));

                itts.talk(soundCommand.getContent(), soundCommand.inputSource);
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
        }
        return true;
    }

    @Override
    public void onTaskComplete(Boolean output) {

    }

    public void stop() {
        itts.stop();
        iMusicPlayer.stop();
        mediaPlayerHelper.stop();
        clearTasks();
    }

    public synchronized boolean isBusy() {
        return itts.isSpeaking() || iMusicPlayer.isPlaying() || mediaPlayerHelper.isPlaying();
    }

    public void destroy() {
        itts.destroy();
        iMusicPlayer.destroy();
        mediaPlayerHelper.destroy();
    }
}
