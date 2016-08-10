package com.geeknewbee.doraemonsdk.output.action;

import android.text.TextUtils;

import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.imscv.NaviPackSdk.NaviPackSdk;

import java.io.IOException;

/**
 * 乐行sdk 实现的脚
 */
public class LeXingFoot implements IFoot {

    public static final int DIRECTION_FORE = 0;
    public static final int DIRECTION_BACK = 1;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_RIGHT = 3;
    public static final int DIRECTION_CLOCKWISE = 4;
    public static final int DIRECTION_EASTERN = 5;
    public static final String LE_XING_DEVICE_NAME_PREFIX = "ttyACM";
    private final static int deviceParam = 115200;
    private NaviPackSdk mNaviPack;
    private int handlerId;

    @Override
    public boolean init() {
        try {
            Process p = Runtime.getRuntime().exec("su chmod 777 /dev/ttyACM0");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String deviceName = "ttyACM0";
//        String deviceName = DeviceUtil.getDeviceName(LE_XING_DEVICE_NAME_PREFIX);
        if (TextUtils.isEmpty(deviceName))
            return false;

        mNaviPack = NaviPackSdk.getInstance();
        handlerId = mNaviPack.createHandler(NaviPackSdk.ConnectTypeEnum.SERIAL_CON);
        int openRet = mNaviPack.open(handlerId, deviceName, deviceParam);
        return openRet == 0;
    }

    @Override
    public boolean setSpeed(int v, int w) {
        return mNaviPack.setSpeed(handlerId, v, w) == 0;
    }

    /**
     * 直线行走
     *
     * @param direction 方向：0 向前，1 向后
     * @param distance  距离
     * @param duration  时间
     * @return 返回值小于0，表示失败，等于0 表示成功
     */
    @Override
    public int setWalkStraight(int direction, int distance, int duration) {
        if (mNaviPack == null) {
            LogUtils.d("setWalkStraight", "The instance of NaviPack is null");
            return -1;
        }
        mNaviPack.setSpeed(0, 0, 0);
        int vSpeed = 0;
        if (DIRECTION_FORE == direction) {
            vSpeed = (int) Math.abs((float) distance / duration);
        } else if (DIRECTION_BACK == direction) {
            vSpeed = (int) -Math.abs((float) distance / duration);
        } else {
            LogUtils.d("setWalkStraight", "Walking direction error...");
            return -2;
        }
        mNaviPack.setSpeed(vSpeed, 0, duration);
        return 0;
    }

    /**
     * 转弯行走
     *
     * @param direction      方向：0 左，1 右
     * @param clockDirection 方式：0 顺时针，1 逆时针
     * @param angle          角度
     * @param radius         半径
     * @param duration       时间
     * @return 返回值小于0，表示失败，等于0 表示成功
     */
    @Override
    public int setTurn(int direction, int clockDirection, int angle, int radius, int duration) {
        if (mNaviPack == null) {
            LogUtils.d("setWalkStraight", "The instance of NaviPack is null");
            return -1;
        }
        mNaviPack.setSpeed(0, 0, 0);
        int vSpeed, wSpeed = 0;
        switch (direction) {
            case DIRECTION_LEFT: // 往左转
                if (DIRECTION_CLOCKWISE == clockDirection) { // 顺时针
                    wSpeed = (int) Math.abs((float) angle / duration);
                    vSpeed = -wSpeed * radius;
                } else if (DIRECTION_EASTERN == clockDirection) { // 逆时针
                    wSpeed = -(int) Math.abs((float) angle / duration);
                    vSpeed = wSpeed * radius;
                } else {
                    LogUtils.d("setTurn", "Clock direction error...");
                    return -3;
                }
                break;
            case DIRECTION_RIGHT: // 往右转
                if (DIRECTION_CLOCKWISE == clockDirection) { // 顺时针
                    wSpeed = (int) Math.abs((float) angle / duration);
                    vSpeed = wSpeed * radius;
                } else if (DIRECTION_EASTERN == clockDirection) { // 逆时针
                    wSpeed = -(int) Math.abs((float) angle / duration);
                    vSpeed = -wSpeed * radius;
                } else {
                    LogUtils.d("setTurn", "Clock direction error...");
                    return -3;
                }
                break;
            default:
                LogUtils.d("setTurn", "Turning direction error...");
                return -2;
        }
        mNaviPack.setSpeed(vSpeed, wSpeed, duration);
        return 0;
    }
}
