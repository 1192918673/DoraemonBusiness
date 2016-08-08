package com.geeknewbee.doraemon.output.action;

import com.geeknewbee.doraemon.processcenter.command.LimbCommandType;

/**
 * 四肢
 * 用于控制四肢运动
 */
public interface IArmsAndHead {
    boolean init();

    boolean send(LimbCommandType limbFunctions, char[] buf);

    boolean send(byte limbFunctions, char[] buf);
}
