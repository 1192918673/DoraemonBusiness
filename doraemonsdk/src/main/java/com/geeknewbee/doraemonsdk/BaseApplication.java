package com.geeknewbee.doraemonsdk;

import android.app.Application;
import android.content.Context;

public abstract class BaseApplication extends Application {
    protected abstract void init();

    public static Context mContext; // 上下文

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        init();
    }
}
