package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.processcenter.ControlType;

public class SwitchControlTypeEvent {
    public ControlType type;

    public SwitchControlTypeEvent(ControlType type) {
        this.type = type;
    }
}
