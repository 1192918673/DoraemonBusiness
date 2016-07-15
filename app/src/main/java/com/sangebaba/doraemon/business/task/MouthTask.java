package com.sangebaba.doraemon.business.task;

import com.sangebaba.doraemon.business.control.IMouth;
import com.sangebaba.doraemon.business.task.base.Priority;
import com.sangebaba.doraemon.business.task.base.PriorityTask;

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
