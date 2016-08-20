package com.geeknewbee.doraemon.output;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.view.MainActivity;
import com.geeknewbee.doraemon.zxing.Encoder;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;

import java.io.IOException;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;

/**
 * 处理表情
 */
public class FaceManager {
    private static volatile FaceManager instance;

    public MainActivity faceActivity;
    private int loopNumber;
    private GifDrawable gifFromResource;
    private AnimationListener animationListener;
    //上次显示的GIF name
    private String lastName;


    private FaceManager() {
        animationListener = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                if (loopNumber == 0)
                    displayGif(lastName, 0); //一直循环现在自己
                else if (loopNumber == 1) {
                    //对应只显示一次的Gif,需要根据当前状态显示不同的表情 正在监听说话、默认两种情况
                    if (Doraemon.getInstance(App.mContext).isListening())
                        displayGif("eyegif_fa_dai", 0);
                    else
                        displayGif("default_gif", 0);
                } else
                    displayGif(lastName, --loopNumber);
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

    public synchronized void displayGif(final String content) {
        displayGif(content, 1);
    }

    public synchronized void displayGif(final String content, int loops) {
        int imageResId = BaseApplication.mContext.getResources().getIdentifier(content, "drawable", BaseApplication.mContext.getPackageName());
        if (imageResId <= 0)
            return;

        loopNumber = loops;
        showGif(content);
    }

    private synchronized void showGif(final String name) {
        faceActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (faceActivity.gifView == null) return;

                int imageResId = BaseApplication.mContext.getResources().getIdentifier(name, "drawable", BaseApplication.mContext.getPackageName());
                if (imageResId > 0) {
                    try {
                        if (!name.equals(lastName) || gifFromResource == null)
                            gifFromResource = new GifDrawable(BaseApplication.mContext.getResources(), imageResId);

                        lastName = name;
                        gifFromResource.removeAnimationListener(animationListener);
                        gifFromResource.setLoopCount(loopNumber);
                        gifFromResource.addAnimationListener(animationListener);
                        faceActivity.gifView.setImageDrawable(gifFromResource);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    public void showQR(final String content) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    return new Encoder.Builder()
                            .setBackgroundColor(0xFFFFFF)
                            .setCodeColor(0xFF000000)
                            .setOutputBitmapPadding(0)
                            .setOutputBitmapWidth(DeviceUtil.dip2px(App.mContext, 100))
                            .setOutputBitmapHeight(DeviceUtil.dip2px(App.mContext, 100))
                            .build().encode(content);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap == null) return;
                faceActivity.imageQR.setImageBitmap(bitmap);
                faceActivity.imageQR.setVisibility(View.VISIBLE);
                faceActivity.gifView.setVisibility(View.INVISIBLE);
            }
        }.execute();
    }

    public void hideQR() {
        faceActivity.imageQR.setVisibility(View.INVISIBLE);
        faceActivity.gifView.setVisibility(View.VISIBLE);
    }
}
