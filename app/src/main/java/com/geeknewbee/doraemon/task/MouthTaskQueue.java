package com.geeknewbee.doraemon.task;


import com.geeknewbee.doraemon.control.base.IMouth;
import com.geeknewbee.doraemon.task.base.Priority;

public class MouthTaskQueue {
    private static IMouth mouth;

    public static void setMouth(IMouth Imouth) {
        mouth = Imouth;
    }

    public static synchronized void addTask(Priority priority, String string) {
        new MouthTask(priority, mouth).execute(string);
    }
}
