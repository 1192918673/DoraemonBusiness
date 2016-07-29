package com.geeknewbee.doraemon;

import android.app.Application;
import android.content.Context;

import com.geeknewbee.doraemon.control.AISpeechAuth;
import com.geeknewbee.doraemon.utils.LogUtils;


public class App extends Application {

    private static final String TAG = App.class.getSimpleName();
    public static Context mContext; // 上下文

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        init();
    }

    private void init() {
        boolean result = new AISpeechAuth().auth();
        LogUtils.d(TAG, "AISpeech auth result:" + result);
    }
}
