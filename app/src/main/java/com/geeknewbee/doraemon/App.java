package com.geeknewbee.doraemon;

import android.app.Application;
import android.content.Context;

import com.geeknewbee.doraemon.control.AISpeechAuth;
import com.geeknewbee.doraemon.control.base.IAuth;


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
