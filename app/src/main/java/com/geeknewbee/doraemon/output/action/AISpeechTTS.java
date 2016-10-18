package com.geeknewbee.doraemon.output.action;

import android.text.TextUtils;

import com.aispeech.AIError;
import com.aispeech.common.AIConstant;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalTTSEngine;
import com.aispeech.export.listeners.AITTSListener;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.processcenter.SyncQueue;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 思必驰 实现 mouth
 */
public class AISpeechTTS implements ITTS {

    private String TAG = AISpeechEar.class.getSimpleName();
    private AILocalTTSEngine mTTSEngine;
    //是否正在讲话
    private boolean isSpeaking;
    private SoundCommand.InputSource inputSource;

    private BlockingQueue<SoundCommand> soundCommands;
    private SoundCommand activeCommand;

    public AISpeechTTS() {
        init();
    }

    private AILocalTTSEngine init() {
        LogUtils.d(TAG, "startInit...");

        if (mTTSEngine != null) {
            mTTSEngine.destroy();
        }
        soundCommands = new ArrayBlockingQueue<SoundCommand>(5);
        mTTSEngine = AILocalTTSEngine.createInstance();//创建实例
        mTTSEngine.setResource("qianran.v2.4.8.bin");//设置使用的合成资源模型名
        mTTSEngine.setDictDbName("aitts_sent_dict_v3.5.db");
        mTTSEngine.setRealBack(true);//设置本地合成使用实时反馈
        mTTSEngine.init(BaseApplication.mContext, new AILocalTTSListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);//初始化合成引擎
        mTTSEngine.setLeftMargin(0);
        mTTSEngine.setRightMargin(0);
        mTTSEngine.setSpeechRate(0.9f);//设置语速
        mTTSEngine.setDeviceId(Util.getIMEI(BaseApplication.mContext));
        LogUtils.d(TAG, "endInit...");

        return mTTSEngine;
    }

    @Override
    public boolean talk(SoundCommand command) {
        LogUtils.d(SyncQueue.TAG, "begin talk");
        this.inputSource = command.inputSource;
        if (TextUtils.isEmpty(command.getContent())) {
            notifyComplete(true, Constants.EMPTY_STRING);
            return true;
        }

        if (mTTSEngine != null) {
            if (isBusy())
                mTTSEngine.stop();
            isSpeaking = true;
            mTTSEngine.speak(command.getContent(), "1024");
        }
        return true;
    }

    @Override
    public boolean stop() {
        soundCommands.clear();
        activeCommand = null;
        if (mTTSEngine != null) {
            mTTSEngine.stop();
            isSpeaking = false;
        }
        return true;
    }

    @Override
    public void addSoundCommand(SoundCommand command) {
        //思必驰有问题暂时不能采用队列，因为会出现tts 不出声没有回调的情况
        if (command.isOverwrite) {
            //清空TTS队列
            soundCommands.clear();
            activeCommand = null;
        }

        soundCommands.offer(command);
        if (activeCommand == null)
            scheduleNext();
    }

    @Override
    public void destroy() {
        if (mTTSEngine != null) {
            mTTSEngine.destroy();
        }
        soundCommands.clear();
        activeCommand = null;
    }

    @Override
    public boolean reInit() {
        mTTSEngine.init(BaseApplication.mContext, new AILocalTTSListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        return true;
    }

    @Override
    public boolean isBusy() {
        return isSpeaking && soundCommands.isEmpty();
    }

    private void scheduleNext() {
        if ((activeCommand = soundCommands.poll()) != null) {
            talk(activeCommand);
        }
    }

    private void notifyComplete(boolean isSuccess, String error) {
        isSpeaking = false;
        EventBus.getDefault().post(new TTSCompleteEvent(inputSource, activeCommand.getId(), isSuccess, error));
        scheduleNext();
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
            LogUtils.d(TAG, "tts onCompletion");
            notifyComplete(true, Constants.EMPTY_STRING);
        }

        @Override
        public void onError(String utteranceId, AIError error) {
            LogUtils.d(TAG, "TTS Error：" + error.getError());
            notifyComplete(false, error.getError());
        }
    }
}
