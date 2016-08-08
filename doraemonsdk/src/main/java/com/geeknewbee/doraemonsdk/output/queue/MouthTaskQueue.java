package com.geeknewbee.doraemonsdk.output.queue;

import com.geeknewbee.doraemonsdk.output.action.AISpeechTTS;
import com.geeknewbee.doraemonsdk.output.action.IMusicPlayer;
import com.geeknewbee.doraemonsdk.output.action.ITTS;
import com.geeknewbee.doraemonsdk.output.action.XMLYMusicPlayer;
import com.geeknewbee.doraemonsdk.processcenter.command.Command;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;

/**
 * 声音 task queue
 */
public class MouthTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    private ITTS itts;
    private IMusicPlayer iMusicPlayer;

    private volatile static MouthTaskQueue instance;

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

    private MouthTaskQueue() {
        super();
        itts = new AISpeechTTS();
        iMusicPlayer = new XMLYMusicPlayer();
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
                itts.talk("正在为您搜索音乐");
                iMusicPlayer.play(input.getContent());
                break;
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
}
