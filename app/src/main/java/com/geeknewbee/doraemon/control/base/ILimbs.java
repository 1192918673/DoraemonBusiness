package com.geeknewbee.doraemon.control.base;

import com.geeknewbee.doraemon.center.command.LimbCommandType;

/**
 * 四肢
 * 用于控制四肢运动
 */
public interface ILimbs {
    boolean init();

    boolean send(LimbCommandType limbFunctions, char[] buf);

    boolean send(byte limbFunctions, char[] buf);
}
