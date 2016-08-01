package com.geeknewbee.doraemon.task;

import android.app.Activity;
import android.os.CountDownTimer;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 处理表情
 */
public class FaceManager {
    //    public static GifView faceView;
    public static GifImageView faceView;
    public static Activity faceActivity;
    private static int duration;

    public static void display(final String content) {
        showGif(content);
    }

    private synchronized static void showGif(final String name) {
        faceActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (faceView == null) return;

                int imageResId = App.mContext.getResources().getIdentifier(name, "drawable", App.mContext.getPackageName());

                if (imageResId > 0) {
                    GifDrawable gifFromResource;
                    try {
                        gifFromResource = new GifDrawable(App.mContext.getResources(), imageResId);
                        if (name.equalsIgnoreCase(Constants.DEFAULT_GIF)) {
                            gifFromResource.setLoopCount(0);
                        } else {
                            gifFromResource.setLoopCount(1);
                        }
                        duration = gifFromResource.getDuration();
                        faceView.setImageDrawable(gifFromResource);

                        new CountDownTimer(duration, duration) {
                            @Override
                            public void onTick(long l) {

                            }

                            @Override
                            public void onFinish() {
                                if (!name.equalsIgnoreCase(Constants.DEFAULT_GIF))
                                    showGif(Constants.DEFAULT_GIF);
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
