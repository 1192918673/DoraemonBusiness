package com.geeknewbee.doraemon.output;

import android.app.Activity;
import android.os.CountDownTimer;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemonsdk.BaseApplication;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 处理表情
 */
public class FaceManager {
    public static GifImageView faceView;
    public static Activity faceActivity;
    public static int loopNumber;

    public synchronized static void display(final String content) {
        display(content, 1);
    }

    public synchronized static void display(final String content, int loops) {
        int imageResId = BaseApplication.mContext.getResources().getIdentifier(content, "drawable", BaseApplication.mContext.getPackageName());
        if (imageResId <= 0)
            return;

        if (loops == 0)
            loopNumber = Integer.MAX_VALUE;
        else
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
                    GifDrawable gifFromResource;
                    try {
                        loopNumber--;
                        gifFromResource = new GifDrawable(BaseApplication.mContext.getResources(), imageResId);
                        int duration = gifFromResource.getDuration();
                        faceView.setImageDrawable(gifFromResource);
                        new CountDownTimer(duration, duration) {
                            @Override
                            public void onTick(long l) {
                            }

                            @Override
                            public void onFinish() {
                                if (loopNumber > 0)
                                    showGif(name);
                                else {
                                    //根据当前状态显示不同的表情 正在监听说话、默认两种情况
                                    if (Doraemon.getInstance(App.mContext).isListening())
                                        display("eyegif_fa_dai", 0);
                                    else
                                        display("default_gif", 0);
                                }
                            }
                        }.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
