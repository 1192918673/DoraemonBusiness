package com.geeknewbee.doraemon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.drawee.view.SimpleDraweeView;
import com.geeknewbee.doraemon.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemon.control.Command;
import com.geeknewbee.doraemon.control.CommandType;
import com.geeknewbee.doraemon.control.Doraemon;
import com.geeknewbee.doraemon.util.Constant;
import com.geeknewbee.doraemon.utils.WifiUtils;


public class MainActivity extends Activity {
    public SimpleDraweeView simpleDraweeView;
    private BluetoothServiceManager bluetoothServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        simpleDraweeView = (SimpleDraweeView) findViewById(R.id.dra_view);
        Doraemon.getInstance(getApplicationContext()).setFace(simpleDraweeView);
        Doraemon.getInstance(getApplicationContext()).addCommand(new Command(CommandType.SHOW_EXPRESSION, Constant.DEFAULT_GIF));

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
