package com.geeknewbee.doraemon.processcenter;

/**
 * 控制模式
 * LOCAL:可以正常的唤醒，监听说话
 * REMOTE:远程控制模式，不能唤醒和监听说话
 * AUTO:自动演示模式
 */
public enum ControlType {
    LOCAL, REMOTE, AUTO
}
