package com.geeknewbee.doraemon.task;


import com.geeknewbee.doraemon.control.AISpeechTTS;
import com.geeknewbee.doraemon.control.Command;
import com.geeknewbee.doraemon.control.XMLYMusicPlayer;
import com.geeknewbee.doraemon.control.base.IMusicPlayer;
import com.geeknewbee.doraemon.control.base.ITTS;
import com.geeknewbee.doraemon.task.base.Priority;
import com.geeknewbee.doraemon.task.base.PriorityTask;

/**
 * 声音task
 */
public class MouthTask extends PriorityTask<Command, Void, Boolean> {
    private static ITTS itts = new AISpeechTTS();
    private static IMusicPlayer iMusicPlayer = new XMLYMusicPlayer();

    public MouthTask(Priority priority) {
        super(priority);
    }

    public static void stop() {
        iMusicPlayer.stop();
    }

    @Override
    protected Boolean performTask(Command... params) {
        Command command = params[0];
        switch (command.getType()) {
            case PLAY_SOUND:
                iMusicPlayer.stop();
                itts.talk(command.getContent());
                break;
            case PLAY_MUSIC:
                iMusicPlayer.stop();
                itts.talk("正在为您搜索音乐");
                iMusicPlayer.play(command.getContent());
                break;
        }
        return true;
    }
}
