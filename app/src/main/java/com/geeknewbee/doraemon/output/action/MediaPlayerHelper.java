package com.geeknewbee.doraemon.output.action;

import android.content.Context;
import android.media.MediaPlayer;

import com.geeknewbee.doraemon.entity.event.DanceMusicStopEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class MediaPlayerHelper {

    private MediaPlayer mediaPlayer;

    public void start(Context context, int rawId) {
        mediaPlayer = MediaPlayer.create(context, rawId);
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
        EventBus.getDefault().post(new MusicCompleteEvent());
        EventBus.getDefault().post(new DanceMusicStopEvent());//通知停止动作
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}
