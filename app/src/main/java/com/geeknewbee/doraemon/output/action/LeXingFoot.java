package com.geeknewbee.doraemon.output.action;

import android.text.TextUtils;

import com.geeknewbee.doraemon.processcenter.LeXingUtil;
import com.geeknewbee.doraemonsdk.utils.DeviceUtil;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.imscv.NaviPackSdk.NaviPackSdk;

import java.io.File;

/**
 * 乐行sdk 实现的脚
 */
public class LeXingFoot implements IFoot {

    public static final String LE_XING_DEVICE_NAME_PREFIX = "ttyACM";
    private final static int deviceParam = 115200;
    public static String TAG = LeXingFoot.class.getSimpleName();
    private NaviPackSdk mNaviPack;
    private int handlerId;
    private boolean initSuccess = false;
    private String lastDeviceName;//上次连接的设备名字

    @Override
    public boolean init() {
        String deviceName = DeviceUtil.getIODeviceName(LE_XING_DEVICE_NAME_PREFIX);
        if (TextUtils.isEmpty(deviceName)) {
            LogUtils.d(TAG, "can not find device");
            return false;
        }
        LogUtils.d(TAG, "foot devices name:" + deviceName);
        deviceName = "/dev/" + deviceName;
        mNaviPack = NaviPackSdk.getInstance();
        handlerId = mNaviPack.createHandler(NaviPackSdk.ConnectTypeEnum.SERIAL_CON);
        int openRet = mNaviPack.open(handlerId, deviceName, deviceParam);
        boolean result = openRet == 0;
        initSuccess = result;
        if (initSuccess)
            lastDeviceName = deviceName;
        return result;
    }

    /**
     * @param v 线速度 mm/s
     * @param w 角速度 毫弧/s
     * @return
     */
    @Override
    public synchronized boolean setSpeed(int v, int w) {
        checkDeviceChange();
        if (mNaviPack == null) {
            LogUtils.d("setWalkStraight", "The instance of NaviPack is null");
            return false;
        }
        return mNaviPack.setSpeed(handlerId, v, w) == 0;
    }

    private void checkDeviceChange() {
        if (!initSuccess)
            init();
        else if (TextUtils.isEmpty(lastDeviceName) || !new File(lastDeviceName).exists()) {
            //如果设备名字发生了变化需要重新init
            mNaviPack.destroy(handlerId);
            handlerId = -1;
            lastDeviceName = null;
            init();
        }
    }

    /**
     * 直线行走
     *
     * @param direction 方向：0 向前，1 向后
     * @param distance  距离  mm
     * @param duration  时间  ms
     * @return 返回值小于0，表示失败，等于0 表示成功
     */
    @Override
    public synchronized int setWalkStraight(LeXingUtil.Direction direction, int distance, int duration) {
        checkDeviceChange();

        if (mNaviPack == null) {
            LogUtils.d("setWalkStraight", "The instance of NaviPack is null");
            return -1;
        }
        stop();
        int[] speed = LeXingUtil.getSpeed(direction, distance, duration);
        mNaviPack.setSpeed(handlerId, speed[0], speed[1]);
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();

        return 0;
    }

    private void stop() {
        if (mNaviPack == null) {
            LogUtils.d("setWalkStraight", "The instance of NaviPack is null");
            return;
        }
        mNaviPack.setSpeed(handlerId, 0, 0);
    }

    /**
     * 转弯行走
     *
     * @param direction      方向：0 左，1 右
     * @param clockDirection 方式：0 顺时针，1 逆时针
     * @param angle          角度  °度
     * @param radius         半径 mm
     * @param duration       时间 ms
     * @return 返回值小于0，表示失败，等于0 表示成功
     */
    @Override
    public synchronized int setTurn(LeXingUtil.Direction direction, LeXingUtil.ClockDirection clockDirection, int angle, int radius, int duration) {
        checkDeviceChange();

        if (mNaviPack == null) {
            LogUtils.d("setWalkStraight", "The instance of NaviPack is null");
            return -1;
        }
        stop();
        int[] speed = LeXingUtil.getSpeed(direction, clockDirection, angle, radius, duration);
        mNaviPack.setSpeed(handlerId, speed[0], speed[1]);
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
        return 0;
    }
}
