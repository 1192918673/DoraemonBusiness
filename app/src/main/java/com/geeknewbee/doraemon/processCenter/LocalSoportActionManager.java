package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.processcenter.command.SportAction;

/**
 * 本地的运动集合，因为现在服务器是返回固定的几个动作，这里和服务做对应。
 * 以后可以让服务器直接发送约定好的命令，直接执行
 */
public class LocalSoportActionManager extends Thread {
    private SportAction head_up;

    {
        head_up = new SportAction();
    }
}
