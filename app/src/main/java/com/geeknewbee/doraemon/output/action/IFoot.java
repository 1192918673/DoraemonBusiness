package com.geeknewbee.doraemon.output.action;

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

    int setWalkStraight(int direction, int speed, int duration);

    int setTurn(int direction, int clockDirection, int angle, int radius, int duration);
}
