package com.geeknewbee.doraemon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.geeknewbee.doraemon.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.control.Command;
import com.geeknewbee.doraemon.control.CommandType;
import com.geeknewbee.doraemon.control.Doraemon;
import com.geeknewbee.doraemon.task.FaceManager;

import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends Activity {
    public GifImageView gifView;
    private BluetoothServiceManager bluetoothServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        gifView = (GifImageView) findViewById(R.id.gifview);
        FaceManager.faceView = gifView;
        FaceManager.faceActivity = this;
        Doraemon.getInstance(getApplicationContext()).addCommand(new Command(CommandType.SHOW_EXPRESSION, Constants.DEFAULT_GIF));

        bluetoothServiceManager = BluetoothServiceManager.getInstance(getApplicationContext());
        bluetoothServiceManager.onCreate();
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
