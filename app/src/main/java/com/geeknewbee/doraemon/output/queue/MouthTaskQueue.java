package com.geeknewbee.doraemon.output.queue;

import com.geeknewbee.doraemon.output.action.AISpeechTTS;
import com.geeknewbee.doraemon.output.action.IMusicPlayer;
import com.geeknewbee.doraemon.output.action.ITTS;
import com.geeknewbee.doraemon.output.action.XMLYMusicPlayer;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;

/**
 * 声音 task queue
 */
public class MouthTaskQueue extends AbstractTaskQueue<Command, Boolean> implements IMusicPlayer.MusicListener {
    private volatile static MouthTaskQueue instance;
    private ITTS itts;
    private IMusicPlayer iMusicPlayer;
    private MouthQueueListener listener;

    private MouthTaskQueue() {
        super();
        itts = new AISpeechTTS();
        iMusicPlayer = new XMLYMusicPlayer();
        iMusicPlayer.setListener(this);
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

    public void setListener(MouthQueueListener listener) {
        this.listener = listener;
    }

    @Override
    public Boolean performTask(Command input) {
        switch (input.getType()) {
            case PLAY_SOUND:
                iMusicPlayer.stop();
                itts.talk(input.getContent());
                break;
            case PLAY_MUSIC:
                iMusicPlayer.stop();
                itts.talk("正在搜索音乐");
                if (input.getContent().equalsIgnoreCase("笑话")) {
                    iMusicPlayer.joke();
                    break;
                } else {
                    iMusicPlayer.play(input.getContent());
                    break;
                }
        }
        return true;
    }

    @Override
    public void onTaskComplete(Boolean output) {

    }

    public void stop() {
        iMusicPlayer.stop();
        clearTasks();
    }

    @Override
    public void onComplete() {
        if (listener != null)
            listener.onTTSComplete();
    }

    public synchronized boolean isPlayMedia() {
        return iMusicPlayer.isPlaying();
    }

    public interface MouthQueueListener {
        void onTTSComplete();
    }
}
