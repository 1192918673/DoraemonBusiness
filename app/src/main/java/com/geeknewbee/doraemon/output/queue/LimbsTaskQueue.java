package com.geeknewbee.doraemon.output.queue;

import com.geeknewbee.doraemon.output.IOutput;
import com.geeknewbee.doraemon.output.action.LimbsManager;
import com.geeknewbee.doraemon.processcenter.command.BluetoothControlFootCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.SportActionSetCommand;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;

/**
 * 四肢和头运动队列
 */
public class LimbsTaskQueue extends AbstractTaskQueue<Command, Boolean> implements IOutput {
    public static final String TAG = LimbsTaskQueue.class.getSimpleName();
    private volatile static LimbsTaskQueue instance;
    private boolean isBusy;

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
    public Boolean performTask(Command command) {
        switch (command.getType()) {
            case SPORT_ACTION_SET:
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
        //
    }

    /**
     * 停止任务
     */
    public void stop() {
        clearTasks();
        LimbsManager.getInstance().stop();
        isBusy = false;
    }

    @Override
    public synchronized boolean isBusy() {
        return isBusy;
    }

    @Override
    public void setBusy(boolean isBusy) {
        this.isBusy = isBusy;
    }

    @Override
    public void addCommand(Command command) {
        addTask(command);
    }

    public void interrupt() {
        LimbsManager.getInstance().stop();
        isBusy = false;
    }
}
