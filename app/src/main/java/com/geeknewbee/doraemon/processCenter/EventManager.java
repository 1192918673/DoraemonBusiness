package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.entity.AuthRobotResponse;
import com.geeknewbee.doraemon.entity.event.BeginningOfSpeechEvent;
import com.geeknewbee.doraemon.entity.event.BeginningofDealWithEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.StartASREvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.entity.event.TranslateSoundCompleteEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * EventBus Event manager
 */
public class EventManager {
    public static void sendStartAsrEvent() {
        EventBus.getDefault().post(new StartASREvent());
    }

    public static void sendMusicCompleteEvent() {
        EventBus.getDefault().post(new MusicCompleteEvent());
    }

    public static void sendBeginningOfSpeechEvent() {
        EventBus.getDefault().post(new BeginningOfSpeechEvent());
    }

    public static void sendBeginningOfDealWithEvent() {
        EventBus.getDefault().post(new BeginningofDealWithEvent());
    }

    public static void sendTranslateSoundComplete() {
        EventBus.getDefault().post(new TranslateSoundCompleteEvent());
    }

    public static void sendHxInfoEvent(AuthRobotResponse.HxUserInfo userInfo) {
        EventBus.getDefault().post(new AuthRobotResponse.HxUserInfo());
    }
}
