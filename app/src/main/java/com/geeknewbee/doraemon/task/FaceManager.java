package com.geeknewbee.doraemon.task;

import android.os.CountDownTimer;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constant;
import com.geeknewbee.doraemon.utils.GifView;

/**
 * 处理表情
 */
public class FaceManager {
    public static GifView faceView;
    private static CountDownTimer countDownTimer;

    public static void display(final String content) {
        showGif(content);
        countDownTimer = new CountDownTimer(1000 * 2, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (!content.equalsIgnoreCase(Constant.DEFAULT_GIF))
                    showGif(Constant.DEFAULT_GIF);
            }
        };
        countDownTimer.start();
    }

    private synchronized static void showGif(String name) {
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
    }
}
