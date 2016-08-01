package com.geeknewbee.doraemon.task;

import android.app.Activity;
import android.os.CountDownTimer;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.view.GifView;

/**
 * 处理表情
 */
public class FaceManager {
    public static GifView faceView;
    public static Activity faceActivity;

    public static void display(final String content) {
        showGif(content);
    }

    private synchronized static void showGif(final String name) {
        faceActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (faceView == null) return;

                int imageResId = App.mContext.getResources().getIdentifier(name, "drawable", App.mContext.getPackageName());
        /*Uri uri = Uri.parse("res://com.geeknewbee.doraemon/" + imageResId);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        Doraemon.getInstance(App.mContext).getFaceView().setController(controller);*/
                if (imageResId > 0)
                    faceView.setMovieResource(imageResId);

                new CountDownTimer(2 * 1000, 2 * 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        if (!name.equalsIgnoreCase(Constants.DEFAULT_GIF))
                            showGif(Constants.DEFAULT_GIF);
                    }
                }.start();
            }
        });
    }
}
