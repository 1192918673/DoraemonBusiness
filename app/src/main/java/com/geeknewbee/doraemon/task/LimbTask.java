package com.geeknewbee.doraemon.task;


import android.text.TextUtils;

import com.geeknewbee.doraemon.control.base.ILimbs;
import com.geeknewbee.doraemon.task.base.Priority;
import com.geeknewbee.doraemon.task.base.PriorityTask;

import java.util.Arrays;

/**
 * 肢体运动task
 */
public class LimbTask extends PriorityTask<String, Void, Boolean> {
    private ILimbs limbs;

    public LimbTask(Priority priority, ILimbs iLimbs) {
        super(priority);
        this.limbs = iLimbs;
    }

    @Override
    protected Boolean performTask(String... params) {
        String content = params[0];

        if (TextUtils.isEmpty(content))
            return false;

        char[] chars = content.toCharArray();
        byte funcationCode = (byte) chars[0];
        char[] contentChar = Arrays.copyOfRange(chars, 1, chars.length);
        boolean send = limbs.send(funcationCode, contentChar);
        return send;
    }
}
