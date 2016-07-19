package com.sangebaba.doraemon.business;

import android.app.Application;
import android.content.Context;

import com.sangebaba.doraemon.business.control.AISpeechAuth;
import com.sangebaba.doraemon.business.control.base.IAuth;

/**
 * Created by ACER on 2016/7/18.
 */
public class App extends Application {

    public static Context mContext; // 上下文
    public IAuth mAuth = new AISpeechAuth();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mAuth.auth();
    }
}
