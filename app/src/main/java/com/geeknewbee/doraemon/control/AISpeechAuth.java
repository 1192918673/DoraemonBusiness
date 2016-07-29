package com.geeknewbee.doraemon.control;

import com.aispeech.export.listeners.AIAuthListener;
import com.aispeech.speech.AIAuthEngine;
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.control.base.IAuth;
import com.geeknewbee.doraemon.utils.LogUtils;

import java.io.FileNotFoundException;

/**
 * Created by ACER on 2016/7/18.
 */
public class AISpeechAuth implements IAuth {

    private String TAG = AISpeechAuth.class.getSimpleName();
    private AIAuthEngine mAuthEngine;

    @Override
    public boolean auth() {
        mAuthEngine = AIAuthEngine.getInstance(App.mContext);// 创建实例
        try {
            mAuthEngine.init(SpeechConstants.APPKEY, SpeechConstants.SECRETKEY, SpeechConstants.APP_CONSTANT);// 初始化
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }// TODO 换成您的s/n码

        mAuthEngine.setOnAuthListener(new AIAuthListener() {// 设置注册监听

            @Override
            public void onAuthSuccess() {
                LogUtils.d(TAG, "注册成功");
            }

            @Override
            public void onAuthFailed(String result) {
                LogUtils.d(TAG, "注册失败：" + result);
            }
        });

        if (mAuthEngine.isAuthed()) {
            return true;
        } else {
            return mAuthEngine.doAuth();// 认证
        }
    }
}
