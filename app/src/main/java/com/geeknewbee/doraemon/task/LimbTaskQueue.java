package com.geeknewbee.doraemon.task;


import com.geeknewbee.doraemon.control.base.ILimbs;
import com.geeknewbee.doraemon.task.base.Priority;

/**
 * 肢体运动队列
 */
public class LimbTaskQueue {
    private static ILimbs limbs;

    public static void setLimbs(ILimbs Imouth) {
        limbs = Imouth;
    }

    public static synchronized void addTask(Priority priority, String string) {
        new LimbTask(priority, limbs).execute(string);
    }
}
