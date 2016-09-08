package com.geeknewbee.doraemon.processcenter;

import android.content.Context;
import android.view.SurfaceView;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.entity.event.ASRResultEvent;
import com.geeknewbee.doraemon.entity.event.BeginningOfSpeechEvent;
import com.geeknewbee.doraemon.entity.event.BeginningofDealWithEvent;
import com.geeknewbee.doraemon.entity.event.LimbActionCompleteEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.NetWorkStateChangeEvent;
import com.geeknewbee.doraemon.entity.event.ReadyForSpeechEvent;
import com.geeknewbee.doraemon.entity.event.ReceiveASRResultEvent;
import com.geeknewbee.doraemon.entity.event.StartASREvent;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.entity.event.TranslateSoundCompleteEvent;
import com.geeknewbee.doraemon.entity.event.WakeupSuccessEvent;
import com.geeknewbee.doraemon.input.AISpeechAuth;
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
import com.geeknewbee.doraemon.processcenter.InputTimeoutMonitorTask.TimeOutMonitorType;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 哆啦A梦 单利模式
 */
public class Doraemon implements IEar.ASRListener, IMessageReceive.MessageListener {
    private volatile static Doraemon instance;
    private final Context context;
    private final InputTimeoutMonitorTask inputTimeOutMonitorTask;
    private final AISpeechAuth speechAuth;
    private IEar ear;
    private IEye eye;
    private IMessageReceive receive;
    private ISoundInputDevice soundInputDevice;
    private Brain brain;
    private double wakePhis = 0;
    //创建一个切换ASR\EDD锁对象
    private Lock switchMonitorLock = new ReentrantLock();
    //创建一个切换AddCommand锁对象
    private Lock addCommandLock = new ReentrantLock();

    private Doraemon(Context context) {
        this.context = context;
        speechAuth = new AISpeechAuth();
        ear = new AISpeechEar();
        eye = ReadSenseEye.getInstance();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void reAuthAndInit(NetWorkStateChangeEvent event) {
        if (!event.isConnected || speechAuth.isAuthed())
            return;

        speechAuth.auth();
        ear.reInit();
        soundInputDevice.reInit();
        MouthTaskQueue.getInstance().reInit();
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
    private void startWakeup() {
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
    private void startASR() {
        ear.startRecognition(wakePhis);
        ear.setASRListener(this);
        addCommand(new ExpressionCommand(Constants.LISTENNING_GIF, 0));
    }

    /**
     * 停止自动语音识别
     */
    private void stopASR() {
        ear.stopRecognition();
        ear.setASRListener(null);
    }

    /**
     * 开始自动人脸识别 Automatic face Recognition
     * @param preView
     */
    public void startAFR(SurfaceView preView) {
        eye.startReadSence(preView);
//        eye.setAFRListener(this);
    }

    /**
     * 停止自动人脸识别
     */
    public void stopAFR() {
//        eye.setAFRListener(null);
        eye.stopReadSence();
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
     * TTS 语音合成完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTTSComplete(TTSCompleteEvent event) {
        //提醒类文本不改变原有状态
        if (event.inputSource == SoundCommand.InputSource.TIPS)
            return;

        //现在唤醒是在提示唤醒词后才开启唤醒
        if (event.inputSource == SoundCommand.InputSource.START_WAKE_UP) {
            if (BuildConfig.HAVE_SPEECH_DEVCE)
                switchSoundMonitor(SoundMonitorType.EDD);
            return;
        }

        //完成后开启语音监听,当在有播放媒体的时候不需要的时候，这里需要处理
        if (isOutputBusy())
            return;

        if (event.inputSource == SoundCommand.InputSource.SOUND_TRANSLATE ||
                event.inputSource == SoundCommand.InputSource.AFTER_WAKE_UP)
            switchSoundMonitor(SoundMonitorType.ASR);
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

        switchSoundMonitor(SoundMonitorType.ASR);
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

        switchSoundMonitor(SoundMonitorType.ASR);
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
        //开启声音等待超时监听
        inputTimeOutMonitorTask.startMonitor(TimeOutMonitorType.MODEL_WAIT_SOUND_INPUT);
    }

    /*
     * ASR监听到开始说话：无语音超时计时结束
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBeginningOfSpeech(BeginningOfSpeechEvent event) {
        LogUtils.d(AISpeechEar.TAG, "onBeginningOfSpeech");
        //显示正在监听Gif
        addCommand(new ExpressionCommand("eyegif_ting", 0));
        //开启声音输入超时监听
        inputTimeOutMonitorTask.startMonitor(TimeOutMonitorType.MODEL_WAIT_SOUND_END);
    }

    /*
     * ASR监听到开始说话：无语音超时计时结束
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onASRResults(ASRResultEvent event) {
        LogUtils.d(AISpeechEar.TAG, "onASRResults");
        //开启声音输入超时监听
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
            switchSoundMonitor(SoundMonitorType.ASR);
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
     * 唤醒成功：停止所有任务、TTS提示语(开启ASR监听)、旋转
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWakeup(WakeupSuccessEvent event) {
        //当唤醒的时候停止当前的动作
        inputTimeOutMonitorTask.stopMonitor();
        this.wakePhis = event.mPhis;
        MouthTaskQueue.getInstance().stop();
        LimbsTaskQueue.getInstance().stop();
        //提示成功 TTS完成后自动打开ASR 这里的类型必须是WAKE_UP
        addCommand(new SoundCommand("唤醒成功", SoundCommand.InputSource.AFTER_WAKE_UP));
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
//        Doraemon.getInstance(App.mContext).addCommand(new LeXingCommand(speed[0], speed[1], 2000));
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
        switchSoundMonitor(event.type);
    }

    private void switchSoundMonitor(SoundMonitorType type) {
        switch (type) {
            case ASR:
                if (BuildConfig.HAVE_SPEECH_DEVCE) {
                    switchMonitorLock.lock();
                    inputTimeOutMonitorTask.stopMonitor();
                    stopWakeUp();
                    stopASR();
                    startASR();
                    switchMonitorLock.unlock();
                }
                break;
            case EDD:
                if (BuildConfig.HAVE_SPEECH_DEVCE) {
                    switchMonitorLock.lock();
                    stopASR();
                    stopWakeUp();
                    startWakeup();
//                    inputTimeOutMonitorTask.startMonitor(TimeOutMonitorType.MODEL_EDD_TIME);
                    switchMonitorLock.unlock();
                }
                break;
        }
    }

    /**
     * 添加指令
     *
     * @param command
     */
    public void addCommand(Command command) {
        addCommandLock.lock();
        brain.addCommand(command);
        addCommandLock.unlock();
    }

    public void addCommand(List<Command> commands) {
        addCommandLock.lock();
        brain.addCommand(commands);
        addCommandLock.unlock();
    }

    public void destroy() {
        ear.destroy();
        soundInputDevice.destroy();
        MouthTaskQueue.getInstance().destroy();
    }
}
