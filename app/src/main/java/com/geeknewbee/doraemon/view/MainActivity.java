package com.geeknewbee.doraemon.view;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.broadcast.BatteryReceiver;
import com.geeknewbee.doraemon.broadcast.NetworkChangeReceiver;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.event.CrashEvent;
import com.geeknewbee.doraemon.entity.event.ReceiveASRResultEvent;
import com.geeknewbee.doraemon.input.ReadSenseService;
import com.geeknewbee.doraemon.input.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.DoraemonInfoManager;
import com.geeknewbee.doraemon.processcenter.LocalResourceManager;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.utils.SensorUtil;
import com.geeknewbee.doraemon.weather.WeatherManager;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends Activity {
    public GifImageView gifView;
    private BluetoothServiceManager bluetoothServiceManager;
    private TextView result;
    private BatteryReceiver receiverBattery;
    private NetworkChangeReceiver receiverNetWork;
    private boolean isLongPress;//是否长按

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(App.TAG, "MainActivity onCreate");
        initView();
        startBluetoothService();
        initData();
        EventBus.getDefault().register(this);
        Doraemon.getInstance(getApplicationContext()).startReceive(); // 开始接受服务器推送消息
        LogUtils.d(ReadSenseService.TAG, "MainActivity 调用。。。");
        Doraemon.getInstance(getApplicationContext()).startAFR();// 开启人脸检测
        registerReceiver();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        event.startTracking();
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                LogUtils.d(App.TAG, "鼻子按下事件");
                if (event.getRepeatCount() == 0) {
                    isLongPress = false;
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                if (isLongPress) {
                    //TODO chang an
                    LogUtils.d(App.TAG, "鼻子事件");
//                    EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
//                    Doraemon.getInstance(getApplicationContext()).addCommand(new SoundCommand("再见，主人，我去休息了", SoundCommand.InputSource.TIPS));
                } else {
                    //TODO duan an
                    LogUtils.d(App.TAG, "鼻子长短按事件");
//                    EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
//                    Doraemon.getInstance(getApplicationContext()).addCommand(new SoundCommand("再见，主人，我去休息了", SoundCommand.InputSource.TIPS));
                }
                isLongPress = false;
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                isLongPress = true;
                return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    private void registerReceiver() {
        IntentFilter filterBattery = new IntentFilter("android.intent.action.BATTERY_CHANGED");
        receiverBattery = new BatteryReceiver();
        registerReceiver(receiverBattery, filterBattery);

        IntentFilter filterNewwork = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        filterNewwork.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        receiverNetWork = new NetworkChangeReceiver();
        registerReceiver(receiverNetWork, filterNewwork);
    }

    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        result = (TextView) findViewById(R.id.tv_result);
        initFaceView();
    }

    private void initFaceView() {
        gifView = (GifImageView) findViewById(R.id.gifview);
        FaceManager.getInstance().faceActivity = this;
        Doraemon.getInstance(getApplicationContext()).addCommand(new ExpressionCommand(Constants.DEFAULT_GIF, 0));
    }

    private void startBluetoothService() {
        bluetoothServiceManager = BluetoothServiceManager.getInstance(getApplicationContext());
        bluetoothServiceManager.init();
        bluetoothServiceManager.start();
    }

    private void initData() {
        //当没有token的时候需要获取token
        DoraemonInfoManager.getInstance(getApplicationContext()).requestTokenFromServer();
        //初始化本地动作库
        LocalResourceManager.getInstance().initLocalAction();
        //开机提示：是否联网
        if (DeviceUtil.isNetworkConnected(getApplicationContext())) {
            Doraemon.getInstance(getApplicationContext()).addCommand(new SoundCommand("呼叫你好小乐，唤醒我", SoundCommand.InputSource.START_WAKE_UP));
        } else {
            Doraemon.getInstance(getApplicationContext()).addCommand(new SoundCommand("网络未连接，请先连接网络", SoundCommand.InputSource.START_WAKE_UP));
        }
        //开机提示：版本检测
        DoraemonInfoManager.getInstance(getApplicationContext()).uploadVersionCode();

        //当WIFI没打开则则要打开WIFI
        DeviceUtil.openWifi(getApplication());

        //注册传感器检测
        SensorUtil.getInstance().initSensor();

        //获取天气预报
        WeatherManager.getInstance().getWeatherReport();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onASRResult(ReceiveASRResultEvent event) {
        //收到ASR的识别结果
        result.setText(event.input);
    }

    /**
     * 当app crash 的时候
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCrash(CrashEvent event) {
        destroy();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverBattery);
        unregisterReceiver(receiverNetWork);
//        unbindService(myServiceConnection);
        destroy();
        Doraemon.getInstance(getApplicationContext()).destroy();
        Doraemon.getInstance(getApplicationContext()).stopAFR();
        WeatherManager.getInstance().destroy();
    }

    private void destroy() {
        if (bluetoothServiceManager != null) {
            bluetoothServiceManager.onDestroy();
            bluetoothServiceManager = null;
        }
    }
}
