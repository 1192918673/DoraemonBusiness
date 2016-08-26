package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.entity.event.BeginningOfSpeechEvent;
import com.geeknewbee.doraemon.entity.event.BeginningofDealWithEvent;
import com.geeknewbee.doraemon.entity.event.LimbActionCompleteEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.ReadyForSpeechEvent;
import com.geeknewbee.doraemon.entity.event.ReceiveASRResultEvent;
import com.geeknewbee.doraemon.entity.event.StartASREvent;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.entity.event.TranslateSoundCompleteEvent;
import com.geeknewbee.doraemon.entity.event.WakeupSuccessEvent;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.input.AISpeechSoundInputDevice;
import com.geeknewbee.doraemon.input.HYMessageReceive;
import com.geeknewbee.doraemon.input.IEar;
import com.geeknewbee.doraemon.input.IEye;
import com.geeknewbee.doraemon.input.IMessageReceive;
import com.geeknewbee.doraemon.input.ISoundInputDevice;
import com.geeknewbee.doraemon.input.ReadSenseEye;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.output.queue.LimbsTaskQueue;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.LeXingCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 哆啦A梦 单利模式
 */
public class Doraemon implements IEar.ASRListener, IEye.AFRListener, IMessageReceive.MessageListener {
    private volatile static Doraemon instance;
    private final Context context;
    private final InputTimeoutMonitorTask inputTimeOutMonitorTask;
    private IEar ear;
    private IEye eye;
    private IMessageReceive receive;
    private ISoundInputDevice soundInputDevice;
    private Brain brain;

    private Doraemon(Context context) {
        this.context = context;
        ear = new AISpeechEar();
        eye = new ReadSenseEye();
        receive = HYMessageReceive.getInstance();
        brain = new Brain();
        soundInputDevice = new AISpeechSoundInputDevice();
        inputTimeOutMonitorTask = new InputTimeoutMonitorTask(context);
        EventBus.getDefault().register(this);
    }

    public static Doraemon getInstance(Context context) {
        if (instance == null) {
            synchronized (Doraemon.class) {
                if (instance == null) {
                    instance = new Doraemon(context);
                }
            }
        }
        return instance;
    }

    /**
     * 是否正在监听声音
     *
     * @return
     */
    public boolean isListening() {
        return ear.isListening();
    }

    /**
     * 启动唤醒  Waiting For Wakeup
     */
    public void startWakeup() {
        soundInputDevice.start();
        addCommand(new ExpressionCommand(Constants.DEFAULT_GIF, 0));
    }

    /**
     * 停止唤醒
     */
    private void stopWakeUp() {
        soundInputDevice.stop();
    }

    /**
     * 开始自动声音识别 Automatic Speech Recognition
     */
    public void startASR() {
        ear.setASRListener(this);
        ear.startRecognition();
        addCommand(new ExpressionCommand(Constants.LISTENNING_GIF, 0));
    }

    /**
     * 停止自动语音识别
     */
    public void stopASR() {
        ear.setASRListener(null);
        ear.stopRecognition();
    }

    /**
     * 开始自动人脸识别 Automatic face Recognition
     */
    public void startAFR() {
        eye.startRecognition();
        eye.setAFRListener(this);
    }

    /**
     * 停止自动人脸识别
     */
    public void stopAFR() {
        eye.setAFRListener(null);
        eye.stopRecognition();
    }

    /**
     * 开始自动接受后台消息 Automatic receive pushData
     */
    public void startReceive() {
        receive.setMessageListener(this);
    }

    /**
     * 语音识别结果
     *
     * @param input     语音识别到的输入文本
     * @param asrOutput 三方的响应结果(如思必驰的库给出的响应信息)
     */
    @Override
    public void onASRResult(String input, String asrOutput, String action, String starName, String musicName) {
        /**
         *
         * 否则需要通过后台服务器进行解析
         */
        LogUtils.d(AISpeechEar.TAG, input + ":" + asrOutput + ":" + action + ":" + starName + ":" + musicName);
        if (BuildConfig.SHOW_ASR_RESULT)
            EventBus.getDefault().post(new ReceiveASRResultEvent(input));
        brain.translateSound(new SoundTranslateInput(input, asrOutput, action, starName, musicName));
    }

    /**
     * 检测到人脸
     */
    @Override
    public void onDetectFace() {

    }

    /**
     * TTS 语音合成完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTTSComplete(TTSCompleteEvent event) {
        //完成后开启语音监听,当在有播放媒体的时候不需要的时候，这里需要处理
        if (isOutputBusy())
            return;

        if (event.inputSource == SoundCommand.InputSource.SOUND_TRANSLATE ||
                event.inputSource == SoundCommand.InputSource.WAKE_UP)
            switchListener(SoundMonitorType.ASR);
    }

    /**
     * 当音乐播放完成(包括 音乐，笑话)
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayMusicComplete(MusicCompleteEvent event) {
        //完成后开启语音监听
        if (isOutputBusy())
            return;

        switchListener(SoundMonitorType.ASR);
    }

    /**
     * 肢体运动完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLimbActionComplete(LimbActionCompleteEvent event) {
        //完成后开启语音监听
        if (isOutputBusy())
            return;

        switchListener(SoundMonitorType.ASR);
    }

    /**
     * 是否正在有输出动作
     *
     * @return
     */
    private boolean isOutputBusy() {
        return MouthTaskQueue.getInstance().isBusy() || LimbsTaskQueue.getInstance().isBusy();
    }

    /**
     * 收到后台推送的消息
     *
     * @param commands
     */
    @Override
    public void onReceivedMessage(List<Command> commands) {
        addCommand(commands);
    }

    /**
     * ASR监听请说话：无语音超时计时开始
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadyForSpeech(ReadyForSpeechEvent event) {
        //开启声音输入超时监听
        inputTimeOutMonitorTask.startMonitor();
    }

    /*
     * ASR监听到开始说话：无语音超时计时开始
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBeginningOfSpeech(BeginningOfSpeechEvent event) {
        LogUtils.d(AISpeechEar.TAG, "onBeginningOfSpeech");
        //显示正在监听Gif
        addCommand(new ExpressionCommand("eyegif_ting", 0));
        inputTimeOutMonitorTask.stopMonitor();
    }

    /**
     * 开启ASR事件：开始声音监听
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startASREvent(StartASREvent event) {
        //完成后开启语音监听
        if (BuildConfig.HAVE_SPEECH_DEVCE)
            startASR();
    }

    /**
     * ASR开始解析声音事件：显示不同表情
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBeginningOfDealWith(BeginningofDealWithEvent event) {
        //正在处理收到的声音指令
        LogUtils.d(AISpeechEar.TAG, "onBeginningOfDealWith");
        addCommand(new ExpressionCommand("eyegif_sikao", 0));
    }

    /**
     * ASR解析声音完成事件：显示不同表情
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTranslateSoundComplete(TranslateSoundCompleteEvent event) {
        //声音输入解析完成 显示默认Gif
        LogUtils.d(AISpeechEar.TAG, "onTranslateSoundComplete");
        addCommand(new ExpressionCommand(Constants.DEFAULT_GIF, 0));
        if (isListening())
            addCommand(new ExpressionCommand(Constants.LISTENNING_GIF, 0));
        else
            addCommand(new ExpressionCommand(Constants.DEFAULT_GIF, 0));
    }

    /**
     * 唤醒成功：停止所有任务、TTS提示语、旋转、开启ASR监听
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWakeup(WakeupSuccessEvent event) {
        //当唤醒的时候停止当前的动作
        MouthTaskQueue.getInstance().stop();
        LimbsTaskQueue.getInstance().stop();
        //提示成功 TTS完成后自动打开ASR 这里的类型必须是WAKE_UP
        addCommand(new SoundCommand("唤醒成功", SoundCommand.InputSource.WAKE_UP));
        //根据声音定位转向
        double turnAngle = 0;
        LeXingUtil.Direction direction;
        LeXingUtil.ClockDirection clockDirection;
        if (event.angle > 180) {
            turnAngle = 360 - event.angle;
            direction = LeXingUtil.Direction.RIGHT;
            clockDirection = LeXingUtil.ClockDirection.CLOCKWISE;
        } else {
            turnAngle = event.angle;
            direction = LeXingUtil.Direction.LEFT;
            clockDirection = LeXingUtil.ClockDirection.EASTERN;
        }
        int[] speed = LeXingUtil.getSpeed(direction, clockDirection, (int) turnAngle, 0, 2000);
        Doraemon.getInstance(App.mContext).addCommand(new LeXingCommand(speed[0], speed[1], 2000));
        //TODO 设置角度
//        mEngine.setDoaChannel(6);//每次都是头对着用户
    }


    /**
     * 切换监听类型
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSwitchMonitorType(SwitchMonitorEvent event) {
        //当input 超时 让声音板休眠 并开启声音监听
        switchListener(event.type);
    }

    private void switchListener(SoundMonitorType type) {
        switch (type) {
            case ASR:
                if (BuildConfig.HAVE_SPEECH_DEVCE) {
                    stopWakeUp();
                    startASR();
                }
                break;
            case EDD:
                if (BuildConfig.HAVE_SPEECH_DEVCE) {
                    stopASR();
                    startWakeup();
                }
                break;
        }
    }

    /**
     * 添加指令
     *
     * @param command
     */
    public synchronized void addCommand(Command command) {
        brain.addCommand(command);
    }

    public synchronized void addCommand(List<Command> commands) {
        brain.addCommand(commands);
    }

}
