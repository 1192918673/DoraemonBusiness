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
        switch (input.getType()) {
            case PLAY_SOUND:
                iMusicPlayer.stop();
                itts.talk(input.getContent());
                break;
            case PLAY_MUSIC:
                iMusicPlayer.stop();
                if (input.getContent().equalsIgnoreCase("笑话")) {
                    iMusicPlayer.joke();
                    break;
                } else {
                    itts.talk("正在为您搜索音乐");
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
}
