package com.geeknewbee.doraemon.task;

import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.control.SDLimbs;
import com.geeknewbee.doraemon.control.base.ILimbs;
import com.geeknewbee.doraemon.utils.LogUtils;

import java.util.Arrays;

/**
 * 肢体运动队列
 */
public class LimbTaskQueue extends AbstractTaskQueue<String, Boolean> {
    private static ILimbs limbs;

    private volatile static LimbTaskQueue instance;

    public static LimbTaskQueue getInstance() {
        if (instance == null) {
            synchronized (LimbTaskQueue.class) {
                if (instance == null) {
                    instance = new LimbTaskQueue();
                }
            }
        }
        return instance;
    }

    private LimbTaskQueue() {
        super();
        limbs = new SDLimbs();
        boolean init = limbs.init();
        LogUtils.d(Constants.TAG_COMMAND, "init limbs:" + init);
    }

    @Override
    public Boolean performTask(String s) {
        if (TextUtils.isEmpty(s))
            return false;

        char[] chars = s.toCharArray();
        byte funcationCode = (byte) chars[0];
        char[] contentChar = Arrays.copyOfRange(chars, 1, chars.length);
        boolean send = limbs.send(funcationCode, contentChar);
        return send;
    }

    @Override
    public void onTaskComplete(Boolean output) {

    }
}
