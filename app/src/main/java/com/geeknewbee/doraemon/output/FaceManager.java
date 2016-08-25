package com.geeknewbee.doraemon.output;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.view.MainActivity;
import com.geeknewbee.doraemon.zxing.Encoder;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.io.IOException;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;

/**
 * 处理表情
 */
public class FaceManager {
    private static volatile FaceManager instance;

    public MainActivity faceActivity;
    private GifDrawable gifFromResource;
    private AnimationListener animationListener;
    //上次显示的GIF name
    private String lastName;
    private int loopNumber;
    private int currentLoop;


    private FaceManager() {
        animationListener = new AnimationListener() {
            @Override
            public void onAnimationCompleted(int loopNumber) {
                LogUtils.d("FaceManager", "onAnimationCompleted loop:" + loopNumber + " name：" + lastName);
                synchronized (FaceManager.this) {
                    if (FaceManager.this.loopNumber != 0)//0为无限循环
                    {
                        currentLoop--;
                        LogUtils.d("FaceManager", "getCurrentLoop:" + currentLoop);
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

    public synchronized void displayGif(final String content) {
        displayGif(content, 1);
    }

    public synchronized void displayGif(final String content, int loops) {
        int imageResId = BaseApplication.mContext.getResources().getIdentifier(content, "drawable", BaseApplication.mContext.getPackageName());
        if (imageResId <= 0)
            return;

        showGif(content, loops);
    }

    private synchronized void showGif(final String name, final int loopNumber) {
        LogUtils.d("FaceManager", "showGif loop:" + loopNumber + " name：" + name);
        faceActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (faceActivity.gifView == null) return;

                int imageResId = BaseApplication.mContext.getResources().getIdentifier(name, "drawable", BaseApplication.mContext.getPackageName());
                if (imageResId > 0) {
                    try {
                        if (!name.equals(lastName) || gifFromResource == null) {
                            if (gifFromResource != null)
                                gifFromResource.recycle();
                            gifFromResource = new GifDrawable(BaseApplication.mContext.getResources(), imageResId);
                        } else
                            gifFromResource.removeAnimationListener(animationListener);

                        synchronized (FaceManager.this) {
                            FaceManager.this.loopNumber = loopNumber;
                            FaceManager.this.currentLoop = loopNumber;
                        }
                        lastName = name;
                        gifFromResource.addAnimationListener(animationListener);
                        gifFromResource.setLoopCount(loopNumber);
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
