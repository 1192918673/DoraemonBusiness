package com.geeknewbee.doraemon.output;

import android.app.Activity;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemonsdk.BaseApplication;

import java.io.IOException;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 处理表情
 */
public class FaceManager {
    public static GifImageView faceView;
    public static Activity faceActivity;
    public static int loopNumber;
    public static GifDrawable gifFromResource;
    private static AnimationListener animationListener;

    public synchronized static void display(final String content) {
        display(content, 1);
    }

    public synchronized static void display(final String content, int loops) {
        int imageResId = BaseApplication.mContext.getResources().getIdentifier(content, "drawable", BaseApplication.mContext.getPackageName());
        if (imageResId <= 0)
            return;

        loopNumber = loops;
        showGif(content);
    }

    private synchronized static void showGif(final String name) {
        faceActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (faceView == null) return;

                int imageResId = BaseApplication.mContext.getResources().getIdentifier(name, "drawable", BaseApplication.mContext.getPackageName());


                if (imageResId > 0) {
                    try {
                        gifFromResource = new GifDrawable(BaseApplication.mContext.getResources(), imageResId);
                        gifFromResource.setLoopCount(loopNumber);

                        animationListener = new AnimationListener() {
                            @Override
                            public void onAnimationCompleted(int loopNumber) {
                                if (loopNumber == 0)
                                    display(name, 0); //一直循环现在自己
                                else if (loopNumber == 1) {
                                    //对应只显示一次的Gif,需要根据当前状态显示不同的表情 正在监听说话、默认两种情况
                                    if (Doraemon.getInstance(App.mContext).isListening())
                                        display("eyegif_fa_dai", 0);
                                    else
                                        display("default_gif", 0);
                                } else
                                    display(name, --loopNumber);
                            }
                        };
                        gifFromResource.addAnimationListener(animationListener);
                        faceView.setImageDrawable(gifFromResource);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
