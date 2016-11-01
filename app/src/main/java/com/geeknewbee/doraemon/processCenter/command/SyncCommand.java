package com.geeknewbee.doraemon.processcenter.command;

import com.geeknewbee.doraemonsdk.task.Priority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class SyncCommand implements Comparable<SyncCommand> {
    private static final int DEFAULT_EXPIRE_TIME = 10 * 1000;
    private Priority priority;
    private List<String> unFinishIDS;
    private Long id;

    /**
     * Current number of elements
     */
    private static final AtomicLong count = new AtomicLong();
    /**
     * 延迟时间 ms
     */
    private int delayTime = 0;
    /**
     * 过期时间 ms
     */
    private int expireTime = DEFAULT_EXPIRE_TIME;
    /**
     * 开始时间戳，默认是是添加时候的时间；对于需要延迟执行的command，根据delayTime来决定，没有到时间不会执行。
     */
    private long startTimestamp;
    /**
     * 过期时间戳，有时候添加的命令的时候，对应的输出通道被占用，不能立即执行，
     * 放在队列中，等输出通道可用的时候再执行。但是有些命令延后执行太久没有什么意义。
     * 例如：正在唱歌的同时设置wifi，wifi会有tips的Sound，如果等歌曲唱完了再说也没有什么意义。
     * 默认：是开始时间后的5s内.可以通过expireTime指定
     */
    private long expireTimestamp;

    //执行命令前是否需要进入EDD模式,现在 只有START_WAKE_UP,AFTER_WAKE_UP 的SoundCommand不需要切换
    public boolean needSwitchEdd = true;

    public List<Command> commandList;

    public SyncCommand(Priority priority, List<Command> commandList, int delayTime, int expireTime, boolean needSwitchEdd) {
        this.id = count.getAndIncrement();
        this.priority = priority;
        this.commandList = commandList;
        this.delayTime = delayTime;
        this.expireTime = expireTime;
        this.startTimestamp = System.currentTimeMillis() + delayTime;
        this.expireTimestamp = startTimestamp + expireTime;
        this.needSwitchEdd = needSwitchEdd;
        unFinishIDS = new ArrayList<>();
        for (Command command : commandList) {
            unFinishIDS.add(command.getId());
        }
    }

    public List<Command> getCommandList() {
        return commandList;
    }

    /**
     * 命令是否已经全部完成
     *
     * @return
     */
    public boolean isComplete() {
        return unFinishIDS.size() == 0;
    }

    /**
     * 执行完成了一个Command
     *
     * @param id
     */
    public synchronized void executeComplete(String id) {
        unFinishIDS.remove(id);
    }

    /**
     * 是否有ID 为 id 的command
     *
     * @param id
     * @return
     */
    public boolean contains(String id) {
        return unFinishIDS.contains(id);
    }


    public Priority getPriority() {
        return priority;
    }

    public long getExpireTimestamp() {
        return expireTimestamp;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
        this.expireTimestamp = startTimestamp + expireTime;
    }

    /**
     * 如果当前事件的优先级更大，则返回-1，如果这两个优先级相等，则返回0，如果当前事件的优先级更小，则返回1。
     *
     * @param another
     * @return
     */
    @Override
    public int compareTo(SyncCommand another) {
        if (this.priority.ordinal() > another.getPriority().ordinal()) {
            return 1;
        } else if (this.priority.ordinal() < another.getPriority().ordinal()) {
            return -1;
        } else {
            return id.compareTo(another.id);
        }
    }

    public static class Builder {
        private Priority priority;
        private int delayTime = 0;
        private int expireTime;
        private boolean needSwitchEdd = true;
        private List<Command> commandList;

        public Builder() {
            priority = Priority.NORMAL;
            expireTime = DEFAULT_EXPIRE_TIME;
        }

        public Builder setPriority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public Builder setDelayTime(int delayTime) {
            this.delayTime = delayTime;
            return this;
        }

        public Builder setExpireTime(int expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public Builder setNeedSwitchEdd(boolean needSwitchEdd) {
            this.needSwitchEdd = needSwitchEdd;
            return this;
        }

        public Builder setCommand(Command command) {
            this.commandList = Collections.singletonList(command);
            return this;
        }

        public Builder setCommandList(List<Command> commandList) {
            this.commandList = commandList;
            return this;
        }

        public SyncCommand build() {
            return new SyncCommand(priority, commandList, delayTime, expireTime, needSwitchEdd);
        }
    }
}
