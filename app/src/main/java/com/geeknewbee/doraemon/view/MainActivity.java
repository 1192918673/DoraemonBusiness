package com.geeknewbee.doraemon.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.database.Weather_City;
import com.geeknewbee.doraemon.processcenter.SoundTranslateTaskQueue;
import com.geeknewbee.doraemon.utils.PaserUtil;
import com.geeknewbee.doraemon.utils.PrefUtils;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.input.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemon.output.FaceManager;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        Doraemon.getInstance(getApplicationContext()).addCommand(new Command(CommandType.WEATHER, "2"));

        bluetoothServiceManager = BluetoothServiceManager.getInstance(getApplicationContext());
        bluetoothServiceManager.onCreate();

        if (!PrefUtils.getBoolean(this, "isDataExist", false)) {
            List<Weather_City> citys = PaserUtil.paserXml(this);
            App.instance.getDaoSession().getWeather_CityDao().insertInTx(citys);
            PrefUtils.saveBoolean(this, "isDataExist", true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Doraemon.getInstance(getApplicationContext()).startASR();
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
