package com.geeknewbee.doraemon.output.action;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.iflytek.speech.setting.TtsSettings;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by GYY on 2016/9/26.
 */
public class XfSpeechTTS implements ITTS {

    private static final String TAG = "XfSpeechTTS";
    private final SharedPreferences mSharedPreferences;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 引擎类型，在线合成或离线合成
    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    // 默认发音人
    private String voicer = "xiaolin";

    private SoundCommand.InputSource inputSource;

    //  是否初始化完成
    private boolean isInit;

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                LogUtils.d(TAG, "初始化失败,错误码：" + code);
            } else {
                isInit = true;
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    public XfSpeechTTS() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(App.mContext, mTtsInitListener);
        mSharedPreferences = App.mContext.getSharedPreferences(TtsSettings.PREFER_NAME, App.mContext.MODE_PRIVATE);
        setParamSynthesis();
    }

    @Override
    public boolean reInit() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(App.mContext, mTtsInitListener);
        return true;
    }

    @Override
    public boolean isSpeaking() {
        return mTts != null && mTts.isSpeaking();
    }

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            LogUtils.d(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            LogUtils.d(TAG, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            LogUtils.d(TAG, "继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {

        }

        @Override
        public void onCompleted(SpeechError error) {
            notifyComplete();
            if (error == null) {
                LogUtils.d(TAG, "播放完成");
            } else {
                LogUtils.d(TAG, error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    @Override
    public boolean talk(String text, SoundCommand.InputSource inputSource) {
        this.inputSource = inputSource;
        if (TextUtils.isEmpty(text)) {
            notifyComplete();
            return true;
        }

        //  如果还没初始化完成，先睡眠再说话
        if (!isInit) {
            try {
                Thread.sleep(1000);
                if (mTts != null) {
                    LogUtils.d(TAG, "第一次开始说话。。。" + text);
                    mTts.startSpeaking(text, mTtsListener);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            LogUtils.d(TAG, "开始说话。。。" + text);
            mTts.startSpeaking(text, mTtsListener);
        }
        return true;
    }

    private void notifyComplete() {
        EventBus.getDefault().post(new TTSCompleteEvent(inputSource));
    }

    @Override
    public boolean stop() {
        if (mTts != null) {
            mTts.stopSpeaking();
        }
        return true;
    }

    @Override
    public void destroy() {
        if (mTts != null) {
            mTts.destroy();
        }
    }

    /**
     * 参数设置
     */
    private void setParamSynthesis() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
//            // 设置在线合成发音人
//            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
//            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        // 设置合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
//        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }
}