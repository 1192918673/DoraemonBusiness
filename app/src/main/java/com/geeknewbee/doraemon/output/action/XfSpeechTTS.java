package com.geeknewbee.doraemon.output.action;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by GYY on 2016/9/26.
 */
public class XfSpeechTTS implements ITTS {

    public static final String TtsSettings_PREFER_NAME = "com.iflytek.setting";
    private static final String TAG = "XfSpeechTTS";
    private final SharedPreferences mSharedPreferences;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 引擎类型，在线合成或离线合成
    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    // 默认发音人
    private String voicer = "nannan";

    private SoundCommand.InputSource inputSource;

    private BlockingQueue<SoundCommand> soundCommands;
    private SoundCommand activeCommand;

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

    private OnTTSCompleteListener onTTSCompleteListener;
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
            if (error == null) {
                LogUtils.d(TAG, "播放完成");
            } else {
                LogUtils.d(TAG, error.getErrorDescription());
            }
            if (onTTSCompleteListener != null) {
                onTTSCompleteListener.onTtsComplete();
                return;
            }
            notifyComplete(error == null, error == null ? Constants.EMPTY_STRING : error.getErrorDescription());
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

    public XfSpeechTTS() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(App.mContext, mTtsInitListener);
        mSharedPreferences = App.mContext.getSharedPreferences(TtsSettings_PREFER_NAME, App.mContext.MODE_PRIVATE);
        setParamSynthesis();
        soundCommands = new ArrayBlockingQueue<SoundCommand>(50);
    }

    public void setOnTTSCompleteListener(OnTTSCompleteListener onTTSCompleteListener) {
        this.onTTSCompleteListener = onTTSCompleteListener;
    }

    @Override
    public boolean reInit() {
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(App.mContext, mTtsInitListener);
        return true;
    }

    @Override
    public boolean isBusy() {
        return mTts != null && mTts.isSpeaking() && soundCommands.isEmpty();
    }

    @Override
    public boolean talk(SoundCommand command) {
        this.inputSource = command.inputSource;
        if (TextUtils.isEmpty(command.getContent())) {
            notifyComplete(true, Constants.EMPTY_STRING);
            return true;
        }
        int code = -1;
        //  如果还没初始化完成，先睡眠再说话
        if (!isInit) {
            try {
                Thread.sleep(1000);
                if (mTts != null) {
                    LogUtils.d(TAG, "第一次开始说话。。。" + command.getContent());
                    code = mTts.startSpeaking(command.getContent(), mTtsListener);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            LogUtils.d(TAG, "开始说话。。。" + command.getContent());
            code = mTts.startSpeaking(command.getContent(), mTtsListener);
        }
        if (code != ErrorCode.SUCCESS) {
            LogUtils.d(TAG, "语音合成失败,错误码: " + code);
        }
        return true;
    }

    private void notifyComplete(boolean isSuccess, String error) {
        if (activeCommand != null)
            EventBus.getDefault().post(new TTSCompleteEvent(inputSource, activeCommand.getId(), isSuccess, error));
        scheduleNext();
    }

    @Override
    public boolean stop() {
        activeCommand = null;
        soundCommands.clear();
        if (mTts != null) {
            mTts.stopSpeaking();
        }
        notifyComplete(true, "");
        return true;
    }

    @Override
    public void addSoundCommand(SoundCommand command) {
        if (command.isOverwrite) {
            //清空TTS队列
            soundCommands.clear();
            activeCommand = null;
            //覆盖上一个tts，需要间隔1000ms
            if (mTts != null && mTts.isSpeaking()) {
                mTts.stopSpeaking();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        soundCommands.offer(command);
        if (activeCommand == null)
            scheduleNext();
    }

    private void scheduleNext() {
        if ((activeCommand = soundCommands.poll()) != null) {
            talk(activeCommand);
        }
    }

    @Override
    public void destroy() {
        if (mTts != null) {
            mTts.destroy();
        }
        soundCommands.clear();
        activeCommand = null;
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
        mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "70"));
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "100"));
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
//        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
//        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    public interface OnTTSCompleteListener {
        void onTtsComplete();
    }
}
