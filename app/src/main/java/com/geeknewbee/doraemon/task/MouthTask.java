package com.geeknewbee.doraemon.task;


import com.geeknewbee.doraemon.control.base.IMouth;
import com.geeknewbee.doraemon.task.base.Priority;
import com.geeknewbee.doraemon.task.base.PriorityTask;

/**
 * 讲话task
 */
public class MouthTask extends PriorityTask<String, Void, Boolean> {
    private IMouth mouth;

    public MouthTask(Priority priority, IMouth mouth) {
        super(priority);
        this.mouth = mouth;
    }

    @Override
    protected Boolean performTask(String... params) {
        return mouth.talk(params[0]);
    }
}
