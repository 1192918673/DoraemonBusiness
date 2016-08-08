package com.geeknewbee.doraemonsdk.output.action;

/**
 * 四肢
 * 用于控制四肢运动
 */
public interface IArmsAndHead {
    boolean init();

    boolean send(byte limbFunctions, char[] buf);
}
