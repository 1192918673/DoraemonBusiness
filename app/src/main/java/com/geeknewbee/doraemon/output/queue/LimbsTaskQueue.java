package com.geeknewbee.doraemon.output.queue;

import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.event.DanceMusicStopEvent;
import com.geeknewbee.doraemon.entity.event.LimbActionCompleteEvent;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.input.bluetooth.ImmediateAlertService;
import com.geeknewbee.doraemon.output.action.IArmsAndHead;
import com.geeknewbee.doraemon.output.action.IFoot;
import com.geeknewbee.doraemon.output.action.LeXingFoot;
import com.geeknewbee.doraemon.output.action.SDArmsAndHead;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.LocalResourceManager;
import com.geeknewbee.doraemon.processcenter.command.BluetoothControlFootCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.SportAction;
import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;
import com.geeknewbee.doraemonsdk.utils.BytesUtils;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 四肢和头运动队列
 */
public class LimbsTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    public static final String TAG = LimbsTaskQueue.class.getSimpleName();
    private volatile static LimbsTaskQueue instance;
    private IArmsAndHead armsAndHead;
    private IFoot foot;
    private boolean isStopAction = false;//跳舞中断标识
    private boolean isBusy = false;
    private boolean isUseLeXing = false;//是否使用乐行
    //为了演示行走过程中手臂一直摆动
    private ArmMoveThread armMoveThread;
    private ReentrantLock reentrantLock = new ReentrantLock();

    private LimbsTaskQueue() {
        super();
        armsAndHead = new SDArmsAndHead();
        boolean init = armsAndHead.init();
        LogUtils.d(Constants.TAG_COMMAND, "init armsAndHead:" + init);

        foot = new LeXingFoot();
        boolean initFoot = foot.init();
        LogUtils.d(Constants.TAG_COMMAND, "init foot:" + initFoot);

        EventBus.getDefault().register(this);
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
    public synchronized void addTask(Command command) {
        if (command instanceof SportActionSetCommand) {
            //添加Task 会覆盖以前当前执行的和任务队列中的任务
            if (((SportActionSetCommand) command).isOverwrite) {
                stop();
            }
        }
        super.addTask(command);
    }

    @Override
    public Boolean performTask(Command command) {
        switch (command.getType()) {
            case SPORT_ACTION_SET:
                EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
                isStopAction = false;
                isBusy = true;
                perform((SportActionSetCommand) command);
                break;
            case BLUETOOTH_CONTROL_FOOT:
                BluetoothControlFootCommand footCommand = (BluetoothControlFootCommand) command;
                perform(footCommand);
                break;
        }

        return true;
    }

    private void perform(BluetoothControlFootCommand command) {
        if (command.v == 0 && command.w == 0) {
            stopMoveThread();
        } else
            startMoveThread();

        if (isUseLeXing)
            sendLeXingFootCommand(command.v, command.w);
        else
            sendLeXingFootCommandByLuGong(command.v, command.w);
    }

    private void startMoveThread() {
        if (armMoveThread == null) {
            reentrantLock.lock();
            if (armMoveThread == null) {
                armMoveThread = new ArmMoveThread();
                armMoveThread.start();
            }
            reentrantLock.unlock();
        }
    }

    private void stopMoveThread() {
        if (armMoveThread != null) {
            reentrantLock.lock();
            if (armMoveThread != null) {
                armMoveThread.cancel();
                armMoveThread.interrupt();
                armMoveThread = null;
            }
            reentrantLock.unlock();
        }
    }


    private void perform(SportActionSetCommand command) {
        if (command.sportActions == null || command.sportActions.isEmpty()) {
            notifyComplete();
            return;
        }

        for (SportAction sportAction : command.sportActions) {
            if (isStopAction) {
                armsAndHead.reset();
                if (isUseLeXing)
                    stopFoot(0);
                else
                    stopFootLuGong(0);
                break;
            }

            if (!TextUtils.isEmpty(sportAction.expressionName))
                Doraemon.getInstance(BaseApplication.mContext).addCommand(new ExpressionCommand(sportAction.expressionName, 3));

            sendTopCommand(sportAction.topCommand);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isUseLeXing) {
                sendLeXingFootCommand(sportAction.footCommand);//暂时采用折中的方案通过路工的中控板控制行走
                stopFoot(sportAction.delayTime);
            } else {
                sendLeXingFootCommandByLuGong(sportAction.footCommand);//暂时采用折中的方案通过路工的中控板控制行走
                stopFootLuGong(sportAction.delayTime);
            }
        }

        notifyComplete();
    }

    private void stopFootLuGong(int delayTime) {
        try {
            Thread.sleep(delayTime);
            sendLeXingFootCommandByLuGong(0, 0);//最后要停止运动
            Thread.sleep(20);
            sendLeXingFootCommandByLuGong(0, 0);//最后要停止运动
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.interrupted();
        }
    }

    private void notifyComplete() {
        isBusy = false;
        EventBus.getDefault().post(new LimbActionCompleteEvent());
    }

    private void sendLeXingFootCommand(String footCommand) {
        if (TextUtils.isEmpty(footCommand))
            return;

        String[] split = footCommand.split("\\|");
        if (split.length != 2) return;
        foot.setSpeed(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
    }

    private boolean sendLeXingFootCommand(int v, int w) {
        return foot.setSpeed(v, w);
    }

    private void stopFoot(int delayTime) {
        try {
            Thread.sleep(delayTime);
            sendLeXingFootCommand(0, 0);//最后要停止运动
            Thread.sleep(20);
            sendLeXingFootCommand(0, 0);//最后要停止运动
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接控制乐行有问题，现在采用暂时方案,先和路工的中控板通信，中控板再发命令到地盘msc
     *
     * @param footCommand
     */
    private boolean sendLeXingFootCommandByLuGong(String footCommand) {
        if (TextUtils.isEmpty(footCommand))
            return false;

        String[] split = footCommand.split("\\|");
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

    private void perform(String s) {
        sendTopCommand(s);
        notifyComplete();
    }

    private Boolean sendTopCommand(String s) {
        if (TextUtils.isEmpty(s)) {
            return false;
        }

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
        if (armMoveThread != null) {
            armMoveThread.cancel();
            armMoveThread.interrupt();
        }
    }

    public synchronized boolean isBusy() {
        return isBusy;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onDanceMusicStop(DanceMusicStopEvent event) {
        //当跳舞的音乐停止 则停止动作
        isStopAction = true;
    }

    /**
     * 手臂前后运动的线程
     */
    private class ArmMoveThread extends Thread {
        private boolean isStopMoveArm = false;
        private final SportActionSetCommand sportActionSetCommand;

        public ArmMoveThread() {
            sportActionSetCommand = LocalResourceManager.getInstance().getActionSetCommand(LocalResourceManager.ACTION_ARM_MOVE);
        }

        @Override
        public void run() {
            LogUtils.d(ImmediateAlertService.TAG, "ArmMoveThread start run");
            super.run();
            while (!isStopMoveArm) {
                performArmMove(sportActionSetCommand);
            }
            armsAndHead.reset();
            LogUtils.d(ImmediateAlertService.TAG, "ArmMoveThread complete");
        }

        @Override
        public synchronized void start() {
            isStopMoveArm = false;
            super.start();
        }

        public void cancel() {
            isStopMoveArm = true;
        }

        private void performArmMove(SportActionSetCommand command) {
            for (SportAction sportAction : command.sportActions) {
                if (isStopMoveArm)
                    break;

                if (!TextUtils.isEmpty(sportAction.expressionName))
                    Doraemon.getInstance(BaseApplication.mContext).addCommand(new ExpressionCommand(sportAction.expressionName, 3));

                sendTopCommand(sportAction.topCommand);
                try {
                    Thread.sleep(sportAction.delayTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
