package com.geeknewbee.doraemon.output.queue;

import android.text.TextUtils;

import com.geeknewbee.doraemon.output.action.IArmsAndHead;
import com.geeknewbee.doraemon.output.action.IFoot;
import com.geeknewbee.doraemon.output.action.LeXingFoot;
import com.geeknewbee.doraemon.output.action.SDArmsAndHead;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.DanceAction;
import com.geeknewbee.doraemon.processcenter.command.DanceCommand;
import com.geeknewbee.doraemon.processcenter.command.LeXingCommand;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.Arrays;

/**
 * 四肢和头运动队列
 */
public class LimbsTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    private volatile static LimbsTaskQueue instance;
    private IArmsAndHead armsAndHead;
    private IFoot foot;
    private boolean isStopDance = false;//跳舞中断标识

    private LimbsTaskQueue() {
        super();
        armsAndHead = new SDArmsAndHead();
        boolean init = armsAndHead.init();
        LogUtils.d(Constants.TAG_COMMAND, "init armsAndHead:" + init);

        foot = new LeXingFoot();
        boolean initFoot = foot.init();
        LogUtils.d(Constants.TAG_COMMAND, "init foot:" + initFoot);
    }

    public static LimbsTaskQueue getInstance() {
        if (instance == null) {
            synchronized (LimbsTaskQueue.class) {
                if (instance == null) {
                    instance = new LimbsTaskQueue();
                }
            }
        }
        return instance;
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
            case LE_XING_FOOT:
                LeXingCommand leXingCommand = (LeXingCommand) command;
                return foot.setSpeed(leXingCommand.v, leXingCommand.w);
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
                Doraemon.getInstance(BaseApplication.mContext).addCommand(new Command(CommandType.SHOW_EXPRESSION, danceAction.expressionName));

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
        boolean send = armsAndHead.send(funcationCode, contentChar);
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
