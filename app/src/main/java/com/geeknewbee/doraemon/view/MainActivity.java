package com.geeknewbee.doraemon.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.event.CrashEvent;
import com.geeknewbee.doraemon.entity.event.ReceiveASRResultEvent;
import com.geeknewbee.doraemon.input.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.DoraemonInfoManager;
import com.geeknewbee.doraemon.processcenter.LocalResourceManager;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.utils.SensorUtil;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends Activity {
    public GifImageView gifView;
    private ImageView imageQRLeft;
    private ImageView imageQRRight;
    public View llQR;
    private BluetoothServiceManager bluetoothServiceManager;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(App.TAG, "MainActivity onCreate");
        initView();
        startBluetoothService();
        initData();
        EventBus.getDefault().register(this);
//        test();
        Doraemon.getInstance(getApplicationContext()).startReceive();
    }

    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        imageQRLeft = (ImageView) findViewById(R.id.iv_left_qr);
        imageQRRight = (ImageView) findViewById(R.id.iv_right_qr);
        result = (TextView) findViewById(R.id.tv_result);
        llQR = findViewById(R.id.ll_qr);

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

    private void test() {
//        findViewById(R.id.bt_test).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DoraemonInfoManager.getInstance(getApplicationContext()).requestTokenFromServer();
//                DoraemonInfoManager.getInstance(getApplicationContext()).uploadBattery(20);
//            }
//        });
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
    }

    public void setQR(Bitmap bitmap) {
        imageQRLeft.setImageBitmap(bitmap);
        imageQRRight.setImageBitmap(bitmap);
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
        destroy();
        Doraemon.getInstance(getApplicationContext()).destroy();
    }

    private void destroy() {
        if (bluetoothServiceManager != null) {
            bluetoothServiceManager.onDestroy();
            bluetoothServiceManager = null;
        }
    }
}
