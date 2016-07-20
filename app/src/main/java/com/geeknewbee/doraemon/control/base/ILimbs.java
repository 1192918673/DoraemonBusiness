package com.geeknewbee.doraemon.control.base;

import com.geeknewbee.doraemon.control.LimbFunction;

/**
 * 四肢
 * 用于控制四肢运动
 */
public interface ILimbs {
    boolean init();

    boolean send(LimbFunction limbFunctions, char[] buf);

    boolean send(byte limbFunctions, char[] buf);
}
