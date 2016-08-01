package com.geeknewbee.doraemon.task;

import android.os.CountDownTimer;
import android.widget.MediaController;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.utils.LogUtils;
import com.geeknewbee.doraemon.view.GifView;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * 处理表情
 */
public class FaceManager {
    //    public static GifView faceView;
    public static GifImageView faceView;
    private static int duration;
    private static CountDownTimer countDownTimer;

    public static void display(final String content) {
        showGif(content);
        countDownTimer = new CountDownTimer(duration, 500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (!content.equalsIgnoreCase(Constants.DEFAULT_GIF))
                    showGif(Constants.DEFAULT_GIF);
            }
        };
        countDownTimer.start();
    }

    private synchronized static void showGif(String name) {
        if (faceView == null) return;

        int imageResId = App.mContext.getResources().getIdentifier(name, "drawable", App.mContext.getPackageName());
        LogUtils.d("图片ID", imageResId + "");
        /*Uri uri = Uri.parse("res://com.geeknewbee.doraemon/" + imageResId);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        Doraemon.getInstance(App.mContext).getFaceView().setController(controller);*/
        if (imageResId > 0) {
//            faceView.setMovieResource(imageResId);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
