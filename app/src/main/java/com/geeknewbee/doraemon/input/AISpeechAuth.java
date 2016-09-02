package com.geeknewbee.doraemon.input;

import com.aispeech.export.listeners.AIAuthListener;
import com.aispeech.speech.AIAuthEngine;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.io.FileNotFoundException;

/**
 * 思必驰认证
 */
public class AISpeechAuth {

    private AIAuthEngine mAuthEngine;

    public AISpeechAuth() {
        // 创建实例
        mAuthEngine = AIAuthEngine.getInstance(BaseApplication.mContext);
    }

    public boolean auth() {
        try {
            mAuthEngine.init(SpeechConstants.APPKEY, SpeechConstants.SECRETKEY, SpeechConstants.APP_CONSTANT);// 初始化
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        mAuthEngine.setOnAuthListener(new AIAuthListener() {// 设置注册监听

            @Override
            public void onAuthSuccess() {
                LogUtils.d(AISpeechEar.TAG, "注册成功");
            }

            @Override
            public void onAuthFailed(String result) {
                LogUtils.d(AISpeechEar.TAG, "注册失败：" + result);
            }
        });

        if (mAuthEngine.isAuthed()) {
            LogUtils.d(AISpeechEar.TAG, "已经注册");
            return true;
        } else {
            return mAuthEngine.doAuth();// 认证
        }
    }

    public boolean isAuthed() {
        return mAuthEngine != null && mAuthEngine.isAuthed();
    }
}
