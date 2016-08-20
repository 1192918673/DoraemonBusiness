package com.geeknewbee.doraemon.output.action;

import com.geeknewbee.doraemonsdk.utils.LogUtils;

public class LeXingUtil {
    public static final int DIRECTION_FORE = 0;
    public static final int DIRECTION_BACK = 1;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_RIGHT = 3;

    //顺时针
    public static final int DIRECTION_CLOCKWISE = 4;
    //逆时针
    public static final int DIRECTION_EASTERN = 5;

    public static int[] getSpeed(int direction, int distance, int duration) {
        int[] result = new int[2];
        int vSpeed = 0;
        if (DIRECTION_FORE == direction) {
            vSpeed = (int) Math.abs((float) distance / duration);
        } else if (DIRECTION_BACK == direction) {
            vSpeed = (int) -Math.abs((float) distance / duration);
        } else {
            LogUtils.d("setWalkStraight", "Walking direction error...");
        }
        result[0] = vSpeed;
        result[1] = 0;
        return result;
    }

    public static int[] getSpeed(int direction, int clockDirection, int angle, int radius, int duration) {
        int[] result = new int[2];
        int vSpeed = 0, wSpeed = 0;
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
                }
                break;
            default:
                LogUtils.d("setTurn", "Turning direction error...");
        }
        result[0] = vSpeed;
        result[1] = wSpeed;
        return result;
    }
}
