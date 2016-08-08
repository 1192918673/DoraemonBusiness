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
public class FootTaskQueue extends AbstractTaskQueue<LeXingCommand, Boolean> {
    private static IFoot foot;

    private volatile static FootTaskQueue instance;

    public static FootTaskQueue getInstance() {
        if (instance == null) {
            synchronized (FootTaskQueue.class) {
                if (instance == null) {
                    instance = new FootTaskQueue();
                }
            }
        }
        return instance;
    }

    private FootTaskQueue() {
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
