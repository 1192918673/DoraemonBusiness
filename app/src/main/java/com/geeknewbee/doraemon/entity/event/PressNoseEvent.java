package com.geeknewbee.doraemon.entity.event;

import com.geeknewbee.doraemon.processcenter.PressNoseType;

public class PressNoseEvent {
    public PressNoseType type;

    public PressNoseEvent(PressNoseType type) {
        this.type = type;
    }
}
