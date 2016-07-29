package com.geeknewbee.doraemon.control;

import com.aispeech.export.listeners.AIAuthListener;
import com.aispeech.speech.AIAuthEngine;
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.utils.LogUtils;

import java.io.FileNotFoundException;

/**
 * 思必驰认证
 */
public class AISpeechAuth {

    private String TAG = AISpeechAuth.class.getSimpleName();

    public boolean auth() {
        AIAuthEngine mAuthEngine = AIAuthEngine.getInstance(App.mContext);// 创建实例
        try {
            mAuthEngine.init(SpeechConstants.APPKEY, SpeechConstants.SECRETKEY, SpeechConstants.APP_CONSTANT);// 初始化
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

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
