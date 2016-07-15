package com.sangebaba.doraemon.business.task;

import com.sangebaba.doraemon.business.control.IMouth;
import com.sangebaba.doraemon.business.task.base.Priority;

public class MouthTaskQueue {
    private static IMouth mouth;

    public static void setMouth(IMouth Imouth) {
        mouth = Imouth;
    }

    public static synchronized void addTask(Priority priority, String string) {
        new MouthTask(priority, mouth).execute(string);
    }
}
