package com.geeknewbee.doraemon.output.queue;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.output.action.IFoot;
import com.geeknewbee.doraemon.output.action.LeXingFoot;
import com.geeknewbee.doraemon.processcenter.command.LeXingCommand;
import com.geeknewbee.doraemon.task.AbstractTaskQueue;
import com.geeknewbee.doraemon.utils.LogUtils;

/**
 * 脚步运动队列
 */
public class FootMoveTaskQueue extends AbstractTaskQueue<LeXingCommand, Boolean> {
    private static IFoot foot;

    private volatile static FootMoveTaskQueue instance;

    public static FootMoveTaskQueue getInstance() {
        if (instance == null) {
            synchronized (FootMoveTaskQueue.class) {
                if (instance == null) {
                    instance = new FootMoveTaskQueue();
                }
            }
        }
        return instance;
    }

    private FootMoveTaskQueue() {
        super();
        foot = new LeXingFoot();
        boolean init = foot.init();
        LogUtils.d(Constants.TAG_COMMAND, "init foot:" + init);
    }

    @Override
    public Boolean performTask(LeXingCommand command) {
        return foot.setSpeed(command.v, command.w);
    }


    @Override
    public void onTaskComplete(Boolean output) {

    }
}
