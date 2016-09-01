package com.geeknewbee.doraemon.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.geeknewbee.doraemon.App;

import java.math.BigDecimal;

/**
 * Created by ACER on 2016/8/16.
 */
public class SensorUtil {

    private static SensorUtil instance;
    public int temperture;
    public int humidity;
    public int light;
    private SensorManager sensorManager;
    private Sensor tempertureSensor;
    private Sensor humiditySensor;
    private Sensor lightSensor;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            switch (event.sensor.getType()) {
                case Sensor.TYPE_TEMPERATURE:
                    // 1.获取温度值
                    temperture = (int) values[0];
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    // 2.获取湿度值
                    float humidityValue = values[0];
                    BigDecimal bd = new BigDecimal(humidityValue);
                    humidity = bd.setScale(2, BigDecimal.ROUND_HALF_UP).intValue();
                    break;
                case Sensor.TYPE_LIGHT:
                    // 3.获取亮度值
                    float lightValue = values[0];
                    BigDecimal bd2 = new BigDecimal(lightValue);
                    light = bd2.setScale(2, BigDecimal.ROUND_HALF_UP).intValue();
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private SensorUtil() {
    }

    public static SensorUtil getInstance() {
        if (instance == null) {
            synchronized (SensorUtil.class) {
                if (instance == null) {
                    instance = new SensorUtil();
                }
            }
        }
        return instance;
    }

    public void initSensor() {
        sensorManager = (SensorManager) App.mContext.getSystemService(App.mContext.SENSOR_SERVICE);
        // 1.温度传感器
        tempertureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        // 2.湿度传感器
        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        // 3.亮度传感器
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        //TODO 判断传感器是否存在

        // 1.注册温度监听
        sensorManager.registerListener(mSensorEventListener, tempertureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        // 2.注册湿度监听
        sensorManager.registerListener(mSensorEventListener, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
        // 3.注册亮度监听
        sensorManager.registerListener(mSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
