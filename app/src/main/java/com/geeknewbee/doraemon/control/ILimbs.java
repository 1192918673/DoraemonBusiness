package com.geeknewbee.doraemon.control;

import com.geeknewbee.doraemon.processCenter.command.LimbCommandType;

/**
 * 四肢
 * 用于控制四肢运动
 */
public interface ILimbs {
    boolean init();

    boolean send(LimbCommandType limbFunctions, char[] buf);

    boolean send(byte limbFunctions, char[] buf);
}
