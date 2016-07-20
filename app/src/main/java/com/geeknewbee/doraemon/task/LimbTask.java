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

    public LimbTask(Priority priority, ILimbs mouth) {
        super(priority);
        this.limbs = mouth;
    }

    @Override
    protected Boolean performTask(String... params) {
        String content = params[0];

        if (TextUtils.isEmpty(content) || content.length() <= 2)
            return false;

        char[] chars = content.toCharArray();
        byte funcationCode = (byte) chars[0];
        char[] contentChar = Arrays.copyOfRange(chars, 1, chars.length);
        return limbs.send(funcationCode, contentChar);
    }
}
