package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.entity.event.BeginningOfSpeechEvent;
import com.geeknewbee.doraemon.entity.event.BeginningofDealWithEvent;
import com.geeknewbee.doraemon.entity.event.InputTimeoutEvent;
import com.geeknewbee.doraemon.entity.event.MusicCompleteEvent;
import com.geeknewbee.doraemon.entity.event.StartASREvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.entity.event.TranslateSoundCompleteEvent;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.input.AISpeechSoundInputDevice;
import com.geeknewbee.doraemon.input.HYMessageReceive;
import com.geeknewbee.doraemon.input.IEar;
import com.geeknewbee.doraemon.input.IEye;
import com.geeknewbee.doraemon.input.IMessageReceive;
import com.geeknewbee.doraemon.input.ISoundInputDevice;
import com.geeknewbee.doraemon.input.ReadSenseEye;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
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
//        inputTimeOutMonitorTask.start();  //TODO 这里还没有连接语音输入板 暂时不起用超时的逻辑
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
     * 开始自动声音识别 Automatic Speech Recognition
     */
    public void startASR() {
        ear.setASRListener(this);
        ear.startRecognition();
        addCommand(new ExpressionCommand("eyegif_fa_dai", 0));
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
        startASR();
    }

    /**
     * 当 音乐播放完成(包括 音乐，笑话)
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayMusicComplete(MusicCompleteEvent event) {
        //完成后开启语音监听
        startASR();
    }

    /**
     * 开始声音监听
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStartASREvent(StartASREvent event) {
        //完成后开启语音监听
        startASR();
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

    /*
     * 开始说话
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBeginningOfSpeech(BeginningOfSpeechEvent event) {
        LogUtils.d(AISpeechEar.TAG, "onBeginningOfSpeech");
        //设置input 监听
        inputTimeOutMonitorTask.setInputFlag();
        //显示正在监听Gif
        addCommand(new ExpressionCommand("eyegif_ting", 0));
    }

    /**
     * 开始处理语音的内容
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
     * 声音输入解析完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTranslateSoundComplete(TranslateSoundCompleteEvent event) {
        //声音输入解析完成 显示默认Gif
        LogUtils.d(AISpeechEar.TAG, "onTranslateSoundComplete");
        addCommand(new ExpressionCommand("default_gif", 0));
        if (isListening())
            addCommand(new ExpressionCommand("eyegif_fa_dai", 0));
        else
            addCommand(new ExpressionCommand("default_gif", 0));
    }

    /**
     * 输入超时
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInputTimeout(InputTimeoutEvent event) {
        //当input 超时 让声音板休眠
        soundInputDevice.sleep();
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
