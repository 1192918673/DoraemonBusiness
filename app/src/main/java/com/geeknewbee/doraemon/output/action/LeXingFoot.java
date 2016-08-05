package com.geeknewbee.doraemon.output.action;

import com.imscv.NaviPackSdk.NaviPackSdk;

/**
 * 乐行sdk 实现的脚
 */
public class LeXingFoot implements IFoot {
    private final static String deviceName = "/dev/ttyACM0";
    private final static int deviceParam = 115200;
    private NaviPackSdk naviPackSdk;
    private int handlerId;

    @Override
    public boolean init() {
        naviPackSdk = NaviPackSdk.getInstance();
        handlerId = naviPackSdk.createHandler(NaviPackSdk.ConnectTypeEnum.SERIAL_CON);
        int openRet = naviPackSdk.open(handlerId, deviceName, deviceParam);
        return openRet == 0;
    }

    @Override
    public boolean setSpeed(int v, int w) {
        return naviPackSdk.setSpeed(handlerId, v, w) == 0;
    }

    @Override
    public boolean walkStraight(int time, int distance, int direction, int priority) {
        return false;
    }

    @Override
    public boolean setTurn(int time, int angle, int radius, int direction, int clockDirection,
                           int priority) {
        return false;
    }
}
