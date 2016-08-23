package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.input.SoundMonitorType;

public class SwitchMonitorEvent {
    public SoundMonitorType type;

    public SwitchMonitorEvent(SoundMonitorType type) {
        this.type = type;
    }
}
