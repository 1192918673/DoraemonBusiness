package com.geeknewbee.doraemon.output.action;

import android.text.TextUtils;

import com.aispeech.AIError;
import com.aispeech.common.AIConstant;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalTTSEngine;
import com.aispeech.export.listeners.AITTSListener;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 思必驰 实现 mouth
 */
public class AISpeechTTS implements ITTS {

    private String TAG = AISpeechEar.class.getSimpleName();
    private AILocalTTSEngine mTTSEngine;
    //是否正在讲话
    private boolean isSpeaking;
    private SoundCommand.InputSource inputSource;

    public AISpeechTTS() {
        init();
    }

    private AILocalTTSEngine init() {
        LogUtils.d(TAG, "startInit...");

        if (mTTSEngine != null) {
            mTTSEngine.destroy();
        }
        mTTSEngine = AILocalTTSEngine.createInstance();//创建实例
        mTTSEngine.setResource("qianran.v2.4.8.bin");
        mTTSEngine.setDictDbName("aitts_sent_dict_v3.5.db");
        mTTSEngine.setRealBack(true);//设置本地合成使用实时反馈
        mTTSEngine.init(BaseApplication.mContext, new AILocalTTSListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);//初始化合成引擎
        mTTSEngine.setLeftMargin(125);
        mTTSEngine.setRightMargin(25);
        mTTSEngine.setSpeechRate(0.85f);//设置语速
        mTTSEngine.setDeviceId(Util.getIMEI(BaseApplication.mContext));
        LogUtils.d(TAG, "endInit...");

        return mTTSEngine;
    }

    @Override
    public synchronized boolean talk(String text, SoundCommand.InputSource inputSource) {
        this.inputSource = inputSource;
        if (TextUtils.isEmpty(text)) {
            notifyComplete();
            return true;
        }

        if (mTTSEngine != null) {
            if (isSpeaking())
                mTTSEngine.stop();
            isSpeaking = true;
            mTTSEngine.speak(text, "1024");
        }
        return true;
    }

    @Override
    public boolean stop() {
        if (mTTSEngine != null) {
            mTTSEngine.stop();
        }
        notifyComplete();
        return true;
    }

    @Override
    public void destroy() {
        if (mTTSEngine != null) {
            mTTSEngine.destroy();
        }
    }

    @Override
    public boolean reInit() {
        mTTSEngine.init(BaseApplication.mContext, new AILocalTTSListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        return true;
    }

    @Override
    public boolean isSpeaking() {
        return isSpeaking;
    }

    private void notifyComplete() {
        isSpeaking = false;
        EventBus.getDefault().post(new TTSCompleteEvent(inputSource));
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
            LogUtils.d(TAG, "tts onReady");
        }

        @Override
        public void onProgress(int currentTime, int totalTime, boolean isRefTextTTSFinished) {
//            LogUtils.d(TAG, "当前播放时间:" + currentTime + "ms, 已经送入内核的文本合成的总时长:" + totalTime + "ms, 是否所有文本合成完成:" + isRefTextTTSFinished);
        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onCompletion(String utteranceId) {
            notifyComplete();
            LogUtils.d(TAG, "tts onCompletion");
        }

        @Override
        public void onError(String utteranceId, AIError error) {
            notifyComplete();
            LogUtils.d(TAG, "TTS Error：" + error.toString());
        }
    }
}
