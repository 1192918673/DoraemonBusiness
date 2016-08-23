package com.geeknewbee.doraemon.output.queue;

import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.action.AISpeechTTS;
import com.geeknewbee.doraemon.output.action.IMusicPlayer;
import com.geeknewbee.doraemon.output.action.ITTS;
import com.geeknewbee.doraemon.output.action.XMLYMusicPlayer;
import com.geeknewbee.doraemon.processcenter.EventManager;
import com.geeknewbee.doraemon.processcenter.command.Command;
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

    private MouthTaskQueue() {
        super();
        itts = new AISpeechTTS();
        iMusicPlayer = new XMLYMusicPlayer();
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

    @Override
    public Boolean performTask(Command input) {
        EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
        switch (input.getType()) {
            case PLAY_SOUND:
                SoundCommand soundCommand = (SoundCommand) input;
                itts.talk(soundCommand.getContent(), soundCommand.inputSource);
                break;
            case PLAY_MUSIC:
                itts.talk("正在搜索音乐", SoundCommand.InputSource.TIPS);
                iMusicPlayer.play(input.getContent());
                EventManager.sendStartAsrEvent();
                break;
            case PLAY_JOKE:
                iMusicPlayer.joke();
                EventManager.sendStartAsrEvent();
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
        clearTasks();
    }

    public synchronized boolean isPlayMedia() {
        return iMusicPlayer.isPlaying();
    }
}
