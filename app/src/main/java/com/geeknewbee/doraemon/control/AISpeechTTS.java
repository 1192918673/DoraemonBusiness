package com.geeknewbee.doraemon.control;

import com.aispeech.AIError;
import com.aispeech.common.AIConstant;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalTTSEngine;
import com.aispeech.export.listeners.AITTSListener;
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.control.base.ITTS;
import com.geeknewbee.doraemon.utils.LogUtils;

/**
 * 思必驰 实现 mouth
 */
public class AISpeechTTS implements ITTS {

    public boolean isSpeaking;// 是否正在说话；如果你的嘴正在说话，当然得先停止正在说的话再让他说新话喽
    private String TAG = AISpeechTTS.class.getSimpleName();
    private AILocalTTSEngine mTTSEngine;

    public AISpeechTTS() {
        init();
    }

    private AILocalTTSEngine init() {
        if (mTTSEngine != null) {
            return mTTSEngine;
        }
        mTTSEngine = AILocalTTSEngine.createInstance();// 创建实例
        mTTSEngine.setResource("tts.zip", "zhilingf.v0.4.11.bin"); // 设置合成资源包和使用的资源模型名
        // mTTSEngine.setModelPath(Environment.getExternalStorageDirectory() + "/ttsRes/" + modelName);// 设置合成模型文件的路径
        mTTSEngine.setRealBack(true);// 设置是否开启实时反馈，默认开启
        mTTSEngine.setUseCahce(false, 20);// 开启本地合成缓存,缓存音频条数为20条,缓存文件在 外存->Android->data->包名->cache->ttsCache目录下
        mTTSEngine.init(App.mContext, new AILocalTTSListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        mTTSEngine.setLeftMargin(125);
        mTTSEngine.setRightMargin(25);
        mTTSEngine.setSpeechRate(0.9f);// 设置语速 取值范围0-10，1为正常语速，10为最快，0为最慢
        mTTSEngine.setDeviceId(Util.getIMEI(App.mContext));// 设置设备Id
        return mTTSEngine;
    }

    @Override
    public boolean talk(String text) {
        if (mTTSEngine != null) {
            /**
             * refText：合成文本；utteranceId：本次合成的ID
             */
            mTTSEngine.speak(text, "1024");
        }
        return true;
    }

    @Override
    public boolean stop() {
        if (mTTSEngine != null) {
            mTTSEngine.stop();
        }
        return true;
    }

    private class AILocalTTSListenerImpl implements AITTSListener {

        @Override
        public void onInit(int status) {
            if (status == AIConstant.OPT_SUCCESS) {
                LogUtils.d(TAG, "本地TTS引擎初始化成功");
            } else {
                LogUtils.d(TAG, "本地TTS引擎初始化失败");
            }
        }

        @Override
        public void onReady(String utteranceId) {
//            LogUtils.d(TAG, utteranceId + "开始播放。。。");
            isSpeaking = true;
        }

        @Override
        public void onProgress(int currentTime, int totalTime, boolean isRefTextTTSFinished) {
//            LogUtils.d(TAG, "当前播放时间:" + currentTime + "ms, 已经送入内核的文本合成的总时长:" + totalTime + "ms, 是否所有文本合成完成:" + isRefTextTTSFinished);
        }

        @Override
        public void onCompletion(String utteranceId) {
//            LogUtils.d(TAG, utteranceId + "播放完毕！");
            isSpeaking = false;
        }

        @Override
        public void onError(String utteranceId, AIError error) {
            LogUtils.d(TAG, "检测到错误：" + error.toString());
        }
    }
}
