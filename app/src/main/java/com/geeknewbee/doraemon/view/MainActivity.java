package com.geeknewbee.doraemon.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.input.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.DoraemonInfoManager;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.umeng.analytics.MobclickAgent;

import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends Activity {
    public GifImageView gifView;
    private BluetoothServiceManager bluetoothServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        startBluetoothService();
        initData();
//        test();
        Doraemon.getInstance(getApplicationContext()).startASR();
    }

    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        initFaceView();
    }

    private void initFaceView() {
        gifView = (GifImageView) findViewById(R.id.gifview);
        FaceManager.faceView = gifView;
        FaceManager.faceActivity = this;
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
        bluetoothServiceManager.onDestroy();
        bluetoothServiceManager = null;
    }
}
