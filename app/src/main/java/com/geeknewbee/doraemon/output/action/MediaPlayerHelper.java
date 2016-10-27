package com.geeknewbee.doraemon.output.action;

import android.content.Context;
import android.media.MediaPlayer;

import com.geeknewbee.doraemon.entity.event.DanceMusicStopEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.processcenter.command.LocalResourceCommand;

import org.greenrobot.eventbus.EventBus;

public class MediaPlayerHelper {

    private MediaPlayer mediaPlayer;
    private LocalResourceCommand activeCommand;

    public void start(Context context, LocalResourceCommand command) {
        activeCommand = command;
        if (command.resourceID <= 0) {
            notifyComplete();
            return;
        }

        stop();

        mediaPlayer = MediaPlayer.create(context, command.resourceID);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                notifyComplete();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                notifyComplete();
                return false;
            }
        });

        mediaPlayer.start();
    }

    private void notifyComplete() {
        mediaPlayer.release();
        if (activeCommand != null)
            EventBus.getDefault().post(new MusicCompleteEvent(activeCommand.getId()));
        EventBus.getDefault().post(new DanceMusicStopEvent());//通知停止动作
    }

    public void stop() {
        if (mediaPlayer != null && isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (activeCommand != null)
            EventBus.getDefault().post(new MusicCompleteEvent(activeCommand.getId()));
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
