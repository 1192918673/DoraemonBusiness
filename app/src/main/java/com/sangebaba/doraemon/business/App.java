package com.sangebaba.doraemon.business;

import android.app.Application;
import android.content.Context;

/**
 * Created by ACER on 2016/7/18.
 */
public class App extends Application {

    public static Context mContext; // 上下文

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}
