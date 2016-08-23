package com.geeknewbee.doraemon.output.queue;

import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.action.IArmsAndHead;
import com.geeknewbee.doraemon.output.action.IFoot;
import com.geeknewbee.doraemon.output.action.LeXingFoot;
import com.geeknewbee.doraemon.output.action.SDArmsAndHead;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.command.ActionSetCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.LeXingCommand;
import com.geeknewbee.doraemon.processcenter.command.SportAction;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;
import com.geeknewbee.doraemonsdk.utils.BytesUtils;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

/**
 * 四肢和头运动队列
 */
public class LimbsTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    private volatile static LimbsTaskQueue instance;
    private IArmsAndHead armsAndHead;
    private IFoot foot;
    private boolean isStopAction = false;//跳舞中断标识

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
        EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
        switch (command.getType()) {
            case ACTIONSET:
                isStopAction = false;
                perform((ActionSetCommand) command);
                break;
            case MECHANICAL_MOVEMENT:
                isStopAction = false;
                return sendCommandContent(command.getContent());
            case LE_XING_FOOT:
                isStopAction = false;
                LeXingCommand leXingCommand = (LeXingCommand) command;
                perform(leXingCommand);
                break;
        }

        return true;
    }

    private void perform(LeXingCommand command) {
        sendLeXingFootCommandByLuGong(command.v, command.w);
        if (command.duration != 0) {
            try {
                Thread.sleep(command.duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendLeXingFootCommandByLuGong(0, 0);//最后要停止运动
        }
    }


    private void perform(ActionSetCommand command) {
        if (command.sportActions == null || command.sportActions.isEmpty())
            return;

        for (SportAction sportAction : command.sportActions) {
            if (isStopAction)
                break;

            if (!TextUtils.isEmpty(sportAction.expressionName))
                Doraemon.getInstance(BaseApplication.mContext).addCommand(new ExpressionCommand(sportAction.expressionName, 1));

            sendCommandContent(sportAction.topCommand);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            sendCommandContent(sportAction.footCommand);
//            sendLeXingFootCommand(sportAction.footCommand);
            sendLeXingFootCommandByLuGong(sportAction.footCommand);//暂时采用折中的方案通过路工的中控板控制行走

            try {
                Thread.sleep(sportAction.delayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            sendLeXingFootCommandByLuGong(0, 0);//最后要停止运动
        }
    }

    private void sendLeXingFootCommand(String footCommand) {
        if (TextUtils.isEmpty(footCommand))
            return;

        String[] split = footCommand.split("|");
        if (split.length != 2) return;
        foot.setSpeed(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
    }

    /**
     * 直接控制乐行有问题，现在采用暂时方案,先和路工的中控板通信，中控板再发命令到地盘msc
     *
     * @param footCommand
     */
    private boolean sendLeXingFootCommandByLuGong(String footCommand) {
        if (TextUtils.isEmpty(footCommand))
            return false;

        String[] split = footCommand.split("|");
        if (split.length != 2) return false;

        return sendLeXingFootCommandByLuGong(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
    }

    private boolean sendLeXingFootCommandByLuGong(int v, int w) {
        byte funcationCode = 0x03;

        char[] charV = BytesUtils.int2bytes(v);
        char[] charW = BytesUtils.int2bytes(w);

        char[] contentChar = new char[]{charV[0], charV[1], charV[2], charV[3], charW[0], charW[1], charW[2], charW[3], 0x00, 0x00, 0x00};
        boolean send = armsAndHead.send(funcationCode, contentChar);
        return send;
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
     * 停止任务
     */
    public void stop() {
        isStopAction = true;
        clearTasks();
    }
}
