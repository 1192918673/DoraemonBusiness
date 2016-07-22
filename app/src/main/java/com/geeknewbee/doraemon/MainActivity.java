package com.geeknewbee.doraemon;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.geeknewbee.doraemon.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemon.utils.WifiUtils;


public class MainActivity extends Activity {
    private BluetoothServiceManager bluetoothServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        bluetoothServiceManager = BluetoothServiceManager.getInstance(getApplicationContext());
        bluetoothServiceManager.onCreate();
        //TODO 测试 以后删除
        WifiUtils.connect(getApplicationContext(), "robot-AP", "robot20161316");
    }

    @Override
    public void onStart() {
        super.onStart();
        bluetoothServiceManager.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        bluetoothServiceManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothServiceManager.onDestroy();
        bluetoothServiceManager = null;
    }
}
