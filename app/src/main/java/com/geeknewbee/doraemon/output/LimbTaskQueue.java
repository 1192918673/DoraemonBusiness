package com.geeknewbee.doraemon.output;

import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.control.base.ILimbs;
import com.geeknewbee.doraemon.processCenter.Doraemon;
import com.geeknewbee.doraemon.processCenter.command.Command;
import com.geeknewbee.doraemon.processCenter.command.CommandType;
import com.geeknewbee.doraemon.processCenter.command.DanceAction;
import com.geeknewbee.doraemon.processCenter.command.DanceCommand;
import com.geeknewbee.doraemon.task.base.AbstractTaskQueue;
import com.geeknewbee.doraemon.utils.LogUtils;

import java.util.Arrays;

/**
 * 肢体运动队列
 */
public class LimbTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    private static ILimbs limbs;
    private boolean isStopDance = false;//跳舞中断标识

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
    public Boolean performTask(Command command) {
        switch (command.getType()) {
            case DANCE:
                isStopDance = false;
                dance((DanceCommand) command);
                break;
            case MECHANICAL_MOVEMENT:
                return sendCommandContent(command.getContent());
        }

        return true;
    }

    private void dance(DanceCommand command) {
        if (command.danceActions == null || command.danceActions.isEmpty())
            return;

        for (DanceAction danceAction : command.danceActions) {
            if (isStopDance)
                break;

            if (!TextUtils.isEmpty(danceAction.expressionName))
                Doraemon.getInstance(App.mContext).addCommand(new Command(CommandType.SHOW_EXPRESSION, danceAction.expressionName));

            sendCommandContent(danceAction.topCommand);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendCommandContent(danceAction.footCommand);

            try {
                Thread.sleep(danceAction.delayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Boolean sendCommandContent(String s) {
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

    /**
     * 停止跳舞
     */
    public void stopDance() {
        isStopDance = true;
    }
}
