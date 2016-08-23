package com.geeknewbee.doraemon.input;

import com.aispeech.AIError;
import com.aispeech.common.AIConstant;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalEddEngine;
import com.aispeech.export.listeners.AILocalEddListener;
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.entity.event.WakeupSuccessEvent;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.LeXingUtil;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.LeXingCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 思必驰声音输入板
 */
public class AISpeechSoundInputDevice implements ISoundInputDevice {

    public static final String TAG = AISpeechEar.TAG;
    private AILocalEddEngine mEngine;
    private boolean isWakeUp;

    public AISpeechSoundInputDevice() {
        init();
    }

    private void init() {
        AIConstant.setUseSpi(true);
        mEngine = AILocalEddEngine.createInstance(); //创建实例
        mEngine.setResBin(SpeechConstants.wakeup_dnn_res);
        mEngine.setDoaCfg(SpeechConstants.uca_config); // 设置声源定位配置文件？
        mEngine.setAecCfg(SpeechConstants.ace_cfg); //
        mEngine.setDoaEnable(true); // 声源定位是否开启
        mEngine.init(App.mContext, new AISpeechListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        mEngine.setStopOnWakeupSuccess(true); // 设置当检测到唤醒词后自动停止唤醒引擎
        mEngine.setWords(new String[]{"你好小乐"}); // 设置唤醒词为小乐，该唤醒词需要与唤醒资源对应
        mEngine.setDeviceId(Util.getIMEI(App.mContext));
    }

    @Override
    public void start() {
        mEngine.start();
        LogUtils.d(TAG, "WakeupEngine start...");
    }

    @Override
    public void stop() {
        mEngine.stop();
        LogUtils.d(TAG, "WakeupEngine stop!!!");
    }

    @Override
    public void setAngle(int angle) {

    }

    @Override
    public synchronized void onWakeUp(double angle) {
        EventBus.getDefault().post(new WakeupSuccessEvent(angle));
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
            onWakeUp(angle);
            // mEngine.start();//如果设置了doa enable为true，在这里start
        }

        @Override
        public void onError(AIError error) {
            LogUtils.d(TAG, "DNNError:" + error.toString());
        }
    }
}
