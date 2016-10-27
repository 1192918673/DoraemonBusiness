package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.processcenter.command.SyncCommand;

import java.util.concurrent.PriorityBlockingQueue;

public class TaskQueue {
    private PriorityBlockingQueue<SyncCommand> syncCommands;

    public TaskQueue() {
    }

    public void addCommand(SyncCommand command) {

    }

    /**
     * 从queue中从前到后查找输入通道没有被占用，可以执行的syncCommand,取出执行
     * 在syncCommand 执行完成时候
     * 在addCommand的时候执行调用
     * syncCommand 有startTime,ExpiresTime。startTime用于执行需要delay的command.ExpiresTime作用是有些命令因为输出通道被占用不能立即执行，给一个过期时间
     * syncCommand有 Priority 0:立即执行会中断当前任务 1:高优先级 2:正常排在队尾
     * 保证每个输出通道在同一时间只会执行一个任务.让一组command都执行完成后才能继续,在新加command的时候如果对应的输出通道没有占用也立即执行.
     * 对与delay 的command 需要在add 的时候开启一个timer ，倒计时后trigger 查找下一个(如果需要倒计时的一定执行当前的command，需要当前command 的优先级最高)
     * <p/>
     * 整体效果是有的命令会打断当前任务，有的可能就会被丢弃,需要对不同的场景的命令定义优先级
     */
    private void scheduleNext() {

    }
}
