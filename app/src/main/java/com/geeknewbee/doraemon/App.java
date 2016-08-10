package com.geeknewbee.doraemon;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.input.AISpeechAuth;
import com.geeknewbee.doraemonsdk.utils.LogUtils;


public class App extends BaseApplication {

    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    protected void init() {
        boolean result = new AISpeechAuth().auth();
        LogUtils.d(TAG, "AISpeech auth result:" + result);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
