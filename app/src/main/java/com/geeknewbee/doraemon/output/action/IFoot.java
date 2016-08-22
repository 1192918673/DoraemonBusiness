package com.geeknewbee.doraemon.output.action;

import com.geeknewbee.doraemon.processcenter.LeXingUtil;

/**
 * 脚步运动接口
 */
public interface IFoot {
    /**
     * 初始化
     *
     * @return
     */
    boolean init();

    /**
     * 设置速度
     *
     * @param v 线速度
     * @param w 角速度
     * @return
     */
    boolean setSpeed(int v, int w);

    int setWalkStraight(LeXingUtil.Direction direction, int speed, int duration);

    int setTurn(LeXingUtil.Direction direction, LeXingUtil.ClockDirection clockDirection, int angle, int radius, int duration);
}
