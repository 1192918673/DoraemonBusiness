package com.geeknewbee.doraemon.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.processcenter.SoundTranslateTaskQueue;
import com.geeknewbee.doraemonsdk.constants.Constants;
import com.geeknewbee.doraemonsdk.input.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemonsdk.output.FaceManager;
import com.geeknewbee.doraemonsdk.processcenter.Doraemon;
import com.geeknewbee.doraemonsdk.processcenter.command.Command;
import com.geeknewbee.doraemonsdk.processcenter.command.CommandType;
import com.umeng.analytics.MobclickAgent;

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
        Doraemon.getInstance(getApplicationContext()).setSoundTranslate(SoundTranslateTaskQueue.getInstance());
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
        MobclickAgent.onResume(this);
        bluetoothServiceManager.onResume();
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
