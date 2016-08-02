package com.geeknewbee.doraemon.output;

import com.geeknewbee.doraemon.center.command.Command;
import com.geeknewbee.doraemon.control.base.IMusicPlayer;
import com.geeknewbee.doraemon.control.base.ITTS;
import com.geeknewbee.doraemon.task.base.AbstractTaskQueue;

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
