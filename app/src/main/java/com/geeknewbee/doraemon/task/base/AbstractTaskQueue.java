package com.geeknewbee.doraemon.task.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.geeknewbee.doraemon.utils.LogUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractTaskQueue<Input, Result> {
    private static final int MESSAGE_POST_RESULT = 0x1;
    private static final String TAG = AbstractTaskQueue.class.getSimpleName();

    private PriorityExecutor executor;
    private InternalHandler handler;
    private WorkerFuture<Result> currentFutureTask;
    private Lock lock;

    protected AbstractTaskQueue() {
        executor = new PriorityExecutor();
        handler = new InternalHandler();
        lock = new ReentrantLock();
    }

    /**
     * 清除所有task
     */
    public void clearTasks() {
        cancelCurrentTask();
        executor.clearAll();
    }

    /**
     * 添加Tadk
     *
     * @param priority
     * @param input
     */
    public synchronized void addTask(Priority priority, final Input input) {
        WorkerRunnable mWorker = new WorkerRunnable<Input, Result>() {
            public Result call() throws Exception {
                lock.lock();
                currentFutureTask = futureTask;
                lock.unlock();

                LogUtils.d(TAG, this.getClass().getSimpleName() + " begin call future");
                mTaskInvoked.set(true);
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                //noinspection unchecked
                return postResult(performTask(mParams));
            }
        };

        WorkerFuture mFuture = new WorkerFuture<Result>(mWorker) {
            @Override
            protected void done() {
                lock.lock();
                LogUtils.d(TAG, this.getClass().getSimpleName() + "future done");
                try {
                    postResultIfNotInvoked(get());
                } catch (InterruptedException e) {
                    android.util.Log.w(TAG, e);
                } catch (ExecutionException e) {
                    throw new RuntimeException("An error occured while executing performTask()",
                            e.getCause());
                } catch (CancellationException e) {
                    postResultIfNotInvoked(null);
                } finally {
                    currentFutureTask = null;
                    lock.unlock();
                }
            }
        };

        mWorker.mParams = input;
        mWorker.futureTask = mFuture;
        executor.execute(new PriorityRunnable(priority, mFuture));
    }

    private void postResultIfNotInvoked(Result result) {
        final boolean wasTaskInvoked = currentFutureTask.workerRunnable.mTaskInvoked.get();
        if (!wasTaskInvoked) {
            postResult(result);
        }
    }

    /**
     * 执行的任务 (线程池执行)
     *
     * @param input
     * @return
     */
    public abstract Result performTask(Input input);

    /**
     * 当执行完成后的回调 UI 线程
     *
     * @param output
     */
    public abstract void onTaskComplete(Result output);//UI thread

    /**
     * 取消当前任务
     */
    public void cancelCurrentTask() {
        if (currentFutureTask != null)
            currentFutureTask.cancel(true);
    }

    protected Result postResult(Result result) {
        Message message = handler.obtainMessage(MESSAGE_POST_RESULT, result);
        message.sendToTarget();
        return result;
    }

    private static abstract class WorkerRunnable<Input, Result> implements Callable<Result> {
        Input mParams;
        WorkerFuture<Result> futureTask;
        AtomicBoolean mCancelled = new AtomicBoolean();
        AtomicBoolean mTaskInvoked = new AtomicBoolean();
    }

    private static class WorkerFuture<Result> extends FutureTask<Result> {
        WorkerRunnable workerRunnable;

        public WorkerFuture(WorkerRunnable callable) {
            super(callable);
            this.workerRunnable = callable;
        }
    }

    private class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    onTaskComplete((Result) msg.obj);
                    break;
            }
        }
    }
}
