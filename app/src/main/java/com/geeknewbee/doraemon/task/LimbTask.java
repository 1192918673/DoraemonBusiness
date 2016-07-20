package com.geeknewbee.doraemon.task;


import com.geeknewbee.doraemon.control.base.ILimbs;
import com.geeknewbee.doraemon.task.base.Priority;
import com.geeknewbee.doraemon.task.base.PriorityTask;

/**
 * 肢体运动task
 */
public class LimbTask extends PriorityTask<String, Void, Boolean> {
    private ILimbs limbs;

    public LimbTask(Priority priority, ILimbs mouth) {
        super(priority);
        this.limbs = mouth;
    }

    @Override
    protected Boolean performTask(String... params) {
        //TODO string解析成 function code  char[]
        byte funcationCode;
        char[] content;
        return limbs.send(funcationCode, content, content.length);
    }
}
