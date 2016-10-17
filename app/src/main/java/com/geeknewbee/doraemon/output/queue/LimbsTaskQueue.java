package com.geeknewbee.doraemon.output.queue;

import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.action.LimbsManager;
import com.geeknewbee.doraemon.processcenter.command.BluetoothControlFootCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;

import org.greenrobot.eventbus.EventBus;

/**
 * 四肢和头运动队列
 */
public class LimbsTaskQueue extends AbstractTaskQueue<Command, Boolean> {
    public static final String TAG = LimbsTaskQueue.class.getSimpleName();
    private volatile static LimbsTaskQueue instance;

    private LimbsTaskQueue() {
        super();
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
                LimbsManager.getInstance().perform((SportActionSetCommand) command);
                break;
            case BLUETOOTH_CONTROL_FOOT:
                BluetoothControlFootCommand footCommand = (BluetoothControlFootCommand) command;
                LimbsManager.getInstance().perform(footCommand);
                break;
        }

        return true;
    }


    @Override
    public void onTaskComplete(Boolean output) {

    }

    /**
     * 停止任务
     */
    public void stop() {
        clearTasks();
        LimbsManager.getInstance().stop();
    }

    public synchronized boolean isBusy() {
        return LimbsManager.getInstance().isBusy();
    }
}
