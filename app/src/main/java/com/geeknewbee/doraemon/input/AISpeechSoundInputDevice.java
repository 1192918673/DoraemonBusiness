package com.geeknewbee.doraemon.input;

import com.aispeech.AIError;
import com.aispeech.common.AIConstant;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalEddEngine;
import com.aispeech.export.listeners.AILocalEddListener;
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.entity.event.WakeupSuccessEvent;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 思必驰声音输入板
 */
public class AISpeechSoundInputDevice implements ISoundInputDevice {

    public static final String TAG = AISpeechEar.TAG;
    private AILocalEddEngine mEngine;
    private boolean isRunning;

    public AISpeechSoundInputDevice() {
        init();
    }

    private void init() {
        mEngine = AILocalEddEngine.createInstance(); //创建实例
        mEngine.setResBin(SpeechConstants.wakeup_dnn_res);
        mEngine.setDoaCfg(SpeechConstants.uca_config);//环形麦的配置
//		mEddEngine.setDoaCfg(SampleConstants.ula_config);//线性麦的配置
//        mEngine.setEchoWavePath(Environment.getExternalStorageDirectory().getPath());
        mEngine.setAecCfg("aec.cfg");
        mEngine.setDoaEnable(true);
        mEngine.init(App.mContext, new AISpeechListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        mEngine.setStopOnWakeupSuccess(true);//设置当检测到唤醒词后自动停止唤醒引擎
        mEngine.setDeviceId(Util.getIMEI(App.mContext));
    }

    @Override
    public boolean reInit() {
        mEngine.init(App.mContext, new AISpeechListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        return true;
    }

    @Override
    public void start() {
//        if (!isRunning) {
        if (mEngine != null && !isRunning) {
            mEngine.start();
            LogUtils.d(TAG, "WakeupEngine start");
        } else
            LogUtils.d(TAG, "WakeupEngine had start");
//        } else {
//            LogUtils.d(TAG, "WakeupEngine had run.");
//        }
        isRunning = true;
    }

    @Override
    public void stop() {
        if (mEngine != null)
            mEngine.stop();
        isRunning = false;
        LogUtils.d(TAG, "WakeupEngine stop");
    }

    @Override
    public void setAngle(int angle) {

    }

    @Override
    public void onWakeUp(double angle, double phis) {
        EventBus.getDefault().post(new WakeupSuccessEvent(angle, phis));
    }

    @Override
    public void destroy() {
        if (mEngine != null) {
            mEngine.destroy();
            mEngine = null;
        }
    }

    private class AISpeechListenerImpl implements AILocalEddListener {

        @Override
        public void onInit(int status) {
            if (status == AIConstant.OPT_SUCCESS) {
                LogUtils.d(TAG, "InitSuccess!status:" + status);
            } else {
                LogUtils.d(TAG, "InitFailed!status:" + status);
            }
        }

        @Override
        public void onReadyForSpeech() {
            LogUtils.d(TAG, "You Can Speak WakeupWord...");
        }

        @Override
        public void onWakeup(String recordId, double confidence, String wakeupWord) {
            LogUtils.d(TAG, "WakeupSuccess recordId:" + recordId + ",confidence:" + confidence + ",wakeupWord:" + wakeupWord + "\n");
            // mEngine.start(); //如果设置了dao enable为false，在这里start
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // LogUtils.d(TAG, "ReceivedData:" + buffer.toString());
        }

        @Override
        public void onDoa(String recordId, double phis, double angle) {
            LogUtils.d(TAG, "DoaSuccess recordId:" + recordId + ",phis:" + phis + ",angle:" + angle + "\n");
            onWakeUp(angle, phis);
            // mEngine.start();//如果设置了doa enable为true，在这里start
        }

        @Override
        public void onError(AIError error) {
            isRunning = false;
            LogUtils.d(TAG, "DNNError:" + error.toString());
        }
    }
}
