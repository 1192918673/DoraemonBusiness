package com.geeknewbee.doraemon.output;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.event.CommandCompleteEvent;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.view.MainActivity;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;

/**
 * 处理表情
 */
public class FaceManager implements IOutput {
    private static volatile FaceManager instance;

    public MainActivity faceActivity;
    private GifDrawable gifFromResource;
    private AnimationListener animationListener;
    //上次显示的GIF name
    private String lastName;
    private int loopNumber;
    private int currentLoop;//当前表情的循环剩余次数

    private FaceManager() {
        animationListener = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                synchronized (FaceManager.this) {
                    if (FaceManager.this.loopNumber != 0)//0为无限循环
                    {
                        currentLoop--;
                        if (currentLoop == 0) {
                            if (Doraemon.getInstance(faceActivity.getApplication()).isListening()) {
                                showGif(Constants.LISTENNING_GIF, 0);
                            } else
                                showGif(Constants.DEFAULT_GIF, 0);
                        }
                    }
                }
            }
        };
    }

    public static FaceManager getInstance() {
        if (instance == null) {
            synchronized (FaceManager.class) {
                if (instance == null)
                    instance = new FaceManager();
            }
        }

        return instance;
    }

    private synchronized void displayGif(final String content, int loops) {
        int imageResId = BaseApplication.mContext.getResources().getIdentifier(content, "drawable", BaseApplication.mContext.getPackageName());
        if (imageResId <= 0)
            return;

        //如果是执行默认的GIF需要等到当前gif显示完成后才能显示（不去影响当前的GIF）
        if (currentLoop != 0 && (content.equals(Constants.LISTENNING_GIF) || content.equals(Constants.DEFAULT_GIF)))
            return;

        showGif(content, loops);
    }

    private synchronized void showGif(final String name, final int loopNumber) {
        LogUtils.d("FaceManager", "showGif loop:" + loopNumber + " name：" + name);
        faceActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (faceActivity.gifView == null) return;

                int imageResId = 0;
                try {
                    imageResId = BaseApplication.mContext.getResources().getIdentifier(name, "drawable", BaseApplication.mContext.getPackageName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (imageResId > 0) {
                    try {
                        if (!name.equals(lastName) || gifFromResource == null) {
                            if (gifFromResource != null) {
                                gifFromResource.removeAnimationListener(animationListener);
                                gifFromResource.recycle();
                            }
                            gifFromResource = new GifDrawable(BaseApplication.mContext.getResources(), imageResId);
                        } else {
                            gifFromResource.removeAnimationListener(animationListener);
                            gifFromResource.stop();
                            gifFromResource.reset();
                        }

                        synchronized (FaceManager.this) {
                            FaceManager.this.loopNumber = loopNumber;
                            FaceManager.this.currentLoop = loopNumber;
                        }
                        lastName = name;
                        gifFromResource.addAnimationListener(animationListener);
                        gifFromResource.setLoopCount(loopNumber);
                        faceActivity.gifView.setImageDrawable(gifFromResource);
                        gifFromResource.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void addCommand(Command command) {
        ExpressionCommand expressionCommand = (ExpressionCommand) command;
        displayGif(expressionCommand.getContent(), expressionCommand.loops);
        notifyComplete(command);
    }

    private void notifyComplete(Command command) {
        EventBus.getDefault().post(new CommandCompleteEvent(command.getId()));
    }

    @Override
    public boolean isBusy() {
        //这些命令都是异步命令，随时可用
        return false;
    }

    @Override
    public void setBusy(boolean isBusy) {
        //该 输出通道一直都是非占用,随时可用
    }
}
