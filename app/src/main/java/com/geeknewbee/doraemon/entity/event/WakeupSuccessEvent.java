package com.geeknewbee.doraemon.entity.event;

public class WakeupSuccessEvent {

    public double angle;
    public double mPhis;

    public WakeupSuccessEvent(double angle, double mPhis) {
        this.angle = angle;
        this.mPhis = mPhis;
    }

}
