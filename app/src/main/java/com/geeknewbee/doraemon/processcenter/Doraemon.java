package com.geeknewbee.doraemon.processcenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.entity.event.ASRResultEvent;
import com.geeknewbee.doraemon.entity.event.BeginningOfSpeechEvent;
import com.geeknewbee.doraemon.entity.event.BeginningofDealWithEvent;
import com.geeknewbee.doraemon.entity.event.NetWorkStateChangeEvent;
import com.geeknewbee.doraemon.entity.event.PressNoseEvent;
import com.geeknewbee.doraemon.entity.event.ReadyForSpeechEvent;
import com.geeknewbee.doraemon.entity.event.ReceiveASRResultEvent;
import com.geeknewbee.doraemon.entity.event.SwitchControlTypeEvent;
import com.geeknewbee.doraemon.entity.event.SyncQueueEmptyEvent;
import com.geeknewbee.doraemon.entity.event.TranslateSoundCompleteEvent;
import com.geeknewbee.doraemon.entity.event.WakeupSuccessEvent;
import com.geeknewbee.doraemon.input.AISpeechAuth;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.input.AISpeechSoundInputDevice;
import com.geeknewbee.doraemon.input.HYMessageReceive;
import com.geeknewbee.doraemon.input.IEar;
import com.geeknewbee.doraemon.input.IMessageReceive;
import com.geeknewbee.doraemon.input.ISoundInputDevice;
import com.geeknewbee.doraemon.input.ReadSenseService;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.input.bluetooth.WirelessControlServiceManager;
import com.geeknewbee.doraemon.output.ReadFaceManager;
import com.geeknewbee.doraemon.output.queue.MouthTaskQueue;
import com.geeknewbee.doraemon.processcenter.InputTimeoutMonitorTask.TimeOutMonitorType;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.processcenter.command.SyncCommand;
import com.geeknewbee.doraemon.weather.WeatherManager;
import com.geeknewbee.doraemonsdk.task.Priority;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 哆啦A梦
 */
public class Doraemon implements IMessageReceive.MessageListener, WirelessControlServiceManager.OnReceiveCommandListener {
    private volatile static Doraemon instance;
    private final Context context;
    private final InputTimeoutMonitorTask inputTimeOutMonitorTask;
    private final AISpeechAuth speechAuth;
    private IEar ear;
    //    private IEye eye;
    private IMessageReceive receive;
    private ISoundInputDevice soundInputDevice;
    private Brain brain;
    //唤醒的角度(思必驰的好像不是角度)
    private double wakePhis = 0;
    //创建一个切换ASR\EDD锁对象
    private Lock switchMonitorLock = new ReentrantLock();
    private ReadSenseTTSReceiver TTSReceiver;
    //控制模式 默认本地控制
    private ControlType controlType = ControlType.LOCAL;
    private WirelessControlServiceManager wirelessControlServiceManager;

    private Doraemon(Context context) {
        this.context = context;
        speechAuth = new AISpeechAuth();
        ear = new AISpeechEar();
//        eye = ReadSenseEye.getInstance();
        receive = HYMessageReceive.getInstance(context);
        brain = new Brain();
        soundInputDevice = new AISpeechSoundInputDevice();
        inputTimeOutMonitorTask = new InputTimeoutMonitorTask(context);
        inputTimeOutMonitorTask.startMonitor(TimeOutMonitorType.MODEL_NONE);
        startBluetoothService();
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

    private void startBluetoothService() {
        wirelessControlServiceManager = WirelessControlServiceManager.getInstance(context);
        wirelessControlServiceManager.setReceiveCommandListener(this);
        wirelessControlServiceManager.init();
        wirelessControlServiceManager.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void netWorkStateChanged(NetWorkStateChangeEvent event) {
        if (!event.isConnected)
            return;

        if (!speechAuth.isAuthed()) {
            speechAuth.auth();
            ear.reInit();
            soundInputDevice.reInit();
            MouthTaskQueue.getInstance().reTTS();
        }
        // 每次重新联网，都重新初始化XMLY
        MouthTaskQueue.getInstance().reMusicPlayer();
        //每次都重新获取天气
        WeatherManager.getInstance().getWeatherReport();
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
    public void stopWakeUp() {
        soundInputDevice.stop();
    }

    /**
     * 开始自动声音识别 Automatic Speech Recognition
     */
    private void startASR() {
        ear.startRecognition(wakePhis);
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
     */
    public void startAFR() {
        Intent intent = new Intent(context, ReadSenseService.class);
        LogUtils.d(ReadSenseService.TAG, "Doraemon 调用。。。");
        context.startService(intent);
        registerTTSReceiver();
    }

    /**
     * 停止自动人脸识别
     */
    public void stopAFR() {
        Intent intent = new Intent(context, ReadSenseService.class);
        context.stopService(intent);
        unRegisterTTSReceiver();
    }

    /**
     * 开始自动接受后台消息 Automatic receive pushData
     */
    public void startReceive() {
        receive.setMessageListener(this);
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
        //停止输入超时监听
        inputTimeOutMonitorTask.stopMonitor();
    }

    /*
     * 当语音输入并解析完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onASRResults(ASRResultEvent event) {
        LogUtils.d(AISpeechEar.TAG, "onASRResults");
        //停止输入超时监听
        if (!event.isSuccess) {
            //如果ASR监听过程出现错误,停止输入操作监听，并重新开启ASR模式
            inputTimeOutMonitorTask.stopMonitor();
            switchSoundMonitor(SoundMonitorType.ASR);
        } else {
            LogUtils.d(AISpeechEar.TAG, "思必驰解析结果完成:" + event.input + ":" + event.asrOutput + ":" + event.action + ":" + event.starName + ":" + event.musicName);
            if (BuildConfig.SHOW_ASR_RESULT)
                EventBus.getDefault().post(new ReceiveASRResultEvent(event.input));
            brain.translateSound(new SoundTranslateInput(event.input, event.asrOutput, event.action, event.starName, event.musicName));
        }
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
     * 鼻子按下事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNosePressed(PressNoseEvent event) {
        this.controlType = ControlType.LOCAL;
        addCommand(new Command(CommandType.STOP));
        switch (event.type) {
            case SHORT_PRESS:
                if (!isListening())
                    addCommand(new SoundCommand(LocalResourceManager.getInstance().getWakeUpString(), SoundCommand.InputSource.AFTER_WAKE_UP));
                break;
            case LONG_PRESS:
                addCommand(new SoundCommand("再见，主人，我去休息了", SoundCommand.InputSource.TIPS));
                switchSoundMonitor(SoundMonitorType.EDD);
                break;
        }
    }

    /**
     * 唤醒成功：停止所有任务、TTS提示语(开启ASR监听)、旋转
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWakeup(WakeupSuccessEvent event) {
        //当唤醒的时候停止当前的动作
        this.wakePhis = event.mPhis;

        List<Command> commandList = new ArrayList<>();
        Command command = new Command(CommandType.STOP);
        commandList.add(command);
        SyncCommand syncCommand = new SyncCommand.Builder().setPriority(Priority.INTERRUPT)
                .setCommandList(commandList).build();
        addCommand(syncCommand);
        addCommand(new SoundCommand(LocalResourceManager.getInstance().getWakeUpString(), SoundCommand.InputSource.AFTER_WAKE_UP));

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

    private void testSyncQueue() {
        addCommand(new Command(CommandType.STOP),
                new SoundCommand(LocalResourceManager.getInstance().getWakeUpString(), SoundCommand.InputSource.AFTER_WAKE_UP));
        addCommand(new SoundCommand("测试下占用的情况1", SoundCommand.InputSource.AFTER_WAKE_UP));
        addCommand(new SoundCommand("测试下占用的情况2", SoundCommand.InputSource.AFTER_WAKE_UP));
        addCommand(new SoundCommand("测试下占用的情况3", SoundCommand.InputSource.AFTER_WAKE_UP));


        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<Command> commandList = new ArrayList<>();
                commandList.add(new SoundCommand("这个是中断的命令", SoundCommand.InputSource.AFTER_WAKE_UP));
                SyncCommand syncCommand = new SyncCommand.Builder().setPriority(Priority.INTERRUPT).setCommandList(commandList).build();
                addCommand(syncCommand);
            }
        }.start();


        List<Command> commandList = new ArrayList<>();
        commandList.add(new SoundCommand("这个是延迟15秒的命令", SoundCommand.InputSource.AFTER_WAKE_UP));
        SyncCommand syncCommand = new SyncCommand.Builder().setCommandList(commandList).setDelayTime(15 * 1000).build();
        addCommand(syncCommand);

        commandList = new ArrayList<>();
        commandList.add(new SoundCommand("在延迟命令之后添加的命令", SoundCommand.InputSource.AFTER_WAKE_UP));
        syncCommand = new SyncCommand.Builder().setCommandList(commandList).setExpireTime(30 * 1000).build();
        addCommand(syncCommand);
    }

    /**
     * 控制模式发生变化
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSwitchControlType(SwitchControlTypeEvent event) {
        if (event.type == ControlType.REMOTE) {
            AutoDemonstrationManager.getInstance(context).stop();
            this.controlType = event.type;
            switchSoundMonitor(SoundMonitorType.CLOSE_ALL);
        } else if (event.type == ControlType.LOCAL) {
            AutoDemonstrationManager.getInstance(context).stop();
            this.controlType = event.type;
            switchSoundMonitor(SoundMonitorType.EDD);
        } else if (event.type == ControlType.AUTO) {
            this.controlType = event.type;
            switchSoundMonitor(SoundMonitorType.CLOSE_ALL);
            AutoDemonstrationManager.getInstance(context).start();
        }
    }

    /**
     * 当SyncQueue 队列任务都完成的时候触发 现在是为了实习自动演示的功能
     * 2016-10-17
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSyncQueueEmpty(SyncQueueEmptyEvent event) {
        if (controlType == ControlType.AUTO)
            AutoDemonstrationManager.getInstance(context).circle();
    }


    public void switchSoundMonitor(SoundMonitorType type) {
        inputTimeOutMonitorTask.stopMonitor();
        switch (type) {
            case ASR:
                //只有LOCAL模式下才可以进入ASR
                if (BuildConfig.HAVE_SPEECH_DEVCE && controlType == ControlType.LOCAL) {
                    switchMonitorLock.lock();
                    stopWakeUp();
                    startASR();
                    switchMonitorLock.unlock();
                }
                break;
            case EDD:
                //只有AUTO模式不能进入EDD
                if (BuildConfig.HAVE_SPEECH_DEVCE && controlType != ControlType.AUTO) {
                    switchMonitorLock.lock();
                    stopASR();
                    startWakeup();
                    switchMonitorLock.unlock();
                }
                break;
            case CLOSE_ALL:
                if (BuildConfig.HAVE_SPEECH_DEVCE) {
                    switchMonitorLock.lock();
                    stopASR();
                    stopWakeUp();
                    addCommand(new ExpressionCommand(Constants.DEFAULT_GIF, 0));
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
        brain.addCommand(command);
    }

    public void addCommand(List<Command> commands) {
        brain.addCommand(commands);
    }

    public void addCommand(Command... commands) {
        brain.addCommand(Arrays.asList(commands));
    }

    public void addCommand(SyncCommand syncCommand) {
        brain.addCommand(syncCommand);
    }

    public void destroy() {
        stopASR();
        stopWakeUp();
        stopAFR();
        wirelessControlServiceManager.onDestroy();
        wirelessControlServiceManager = null;
        ear.destroy();
        soundInputDevice.destroy();
        inputTimeOutMonitorTask.cancel();
        MouthTaskQueue.getInstance().destroy();
        receive.destroy();
        EventBus.getDefault().unregister(this);
        instance = null;
    }

    /**
     * 注册接受ReadSenseService的TTS的广播
     */
    private void registerTTSReceiver() {
        TTSReceiver = new ReadSenseTTSReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.READSENSE_BROADCAST_TIPS_ACTION);
        intentFilter.addAction(Constants.ACTION_DORAEMON_DISCOVERY_PERSON);
        context.registerReceiver(TTSReceiver, intentFilter);
        LogUtils.d(ReadSenseService.TAG, "注册接受播报广播");
    }

    /**
     * 解除接受ReadSenseService的TTS的广播
     */
    private void unRegisterTTSReceiver() {
        try {
            context.unregisterReceiver(TTSReceiver);
            TTSReceiver = null;
            LogUtils.d(ReadSenseService.TAG, "解除注册接受播报广播");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 当接收到无线远程的命令
     *
     * @param commands
     */
    @Override
    public void onReceiveCommand(List<Command> commands) {
        addCommand(commands);
    }

    class ReadSenseTTSReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.ACTION_DORAEMON_DISCOVERY_PERSON:
                    int personId = intent.getIntExtra(Constants.EXTRA_PERSON_ID, 0);
                    String personName = ReadFaceManager.getInstance(context).getPersonName(personId);
                    if (!TextUtils.isEmpty(personName))
                        addCommand(new SyncCommand.Builder().setPriority(Priority.INTERRUPT)
                                .setCommand(new SoundCommand(personName + "你好!", SoundCommand.InputSource.TIPS)).build());
                    break;
                case Constants.READSENSE_BROADCAST_TIPS_ACTION:
                    LogUtils.d(ReadSenseService.TAG, "收到ReadSense的播报广播");
                    addCommand(new SoundCommand(intent.getStringExtra("text"), SoundCommand.InputSource.TIPS));
                    break;
            }
        }
    }
}
