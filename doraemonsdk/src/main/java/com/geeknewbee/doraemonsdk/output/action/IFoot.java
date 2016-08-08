package com.geeknewbee.doraemonsdk.output.action;

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

    boolean walkStraight(int time, int distance, int direction, int priority);

    boolean setTurn(int time, int angle, int radius, int direction, int clockDirection, int priority);
}
