package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.StartASREvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * EventBus Event manager
 */
public class EventManager {
    public static void sendStartAsrEvent() {
        EventBus.getDefault().post(new StartASREvent());
    }

    public static void sendTTSCompleteEvent() {
        EventBus.getDefault().post(new TTSCompleteEvent());
    }

    public static void sendMusicCompleteEvent() {
        EventBus.getDefault().post(new MusicCompleteEvent());
    }
}
