package com.sangebaba.doraemon.business.task;

import com.sangebaba.doraemon.business.control.IMouth;

public class MouthTask extends PriorityAsyncTask<String, Void, Boolean> {
    private IMouth mouth;

    public MouthTask(Priority priority, IMouth mouth) {
        super(priority);
        this.mouth = mouth;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        return mouth.talk(params[0]);
    }
}
