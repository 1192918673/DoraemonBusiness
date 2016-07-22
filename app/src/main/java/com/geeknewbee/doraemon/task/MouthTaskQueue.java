package com.geeknewbee.doraemon.task;


import com.geeknewbee.doraemon.control.Command;
import com.geeknewbee.doraemon.task.base.Priority;

/**
 * 声音 task queue
 */
public class MouthTaskQueue {

    public static synchronized void addTask(Priority priority, Command command) {
        new MouthTask(priority).execute(command);
    }

    public static void stop() {
        MouthTask.stop();
    }
}
