package com.geeknewbee.doraemonsdk;

import android.app.Application;
import android.content.Context;

public abstract class BaseApplication extends Application {
    public static Context mContext; // 上下文

    protected abstract void init();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
