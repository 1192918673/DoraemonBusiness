package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemonsdk.utils.LogUtils;

public class LeXingUtil {
    /**
     * 获取线速度，角速度
     *
     * @param direction
     * @param distance  距离 mm(毫米)
     * @param duration  时间 ms(毫秒)
     * @return
     */
    public static int[] getSpeed(Direction direction, int distance, int duration) {
        int[] result = new int[2];
        int vSpeed = 0;
        switch (direction) {
            case FORE:
                vSpeed = (int) Math.abs((float) distance / duration * 1000);
                break;
            case BACK:
                vSpeed = (int) -Math.abs((float) distance / duration * 1000);
                break;
        }
        result[0] = vSpeed;
        result[1] = 0;
        return result;
    }

    /**
     * 获取线速度，角速度
     *
     * @param direction
     * @param clockDirection
     * @param angle          角度  °(度)
     * @param radius         半径  mm(毫米)
     * @param duration       时间  ms(毫秒)
     * @return
     */
    public static int[] getSpeed(Direction direction, ClockDirection clockDirection, int angle, int radius, int duration) {
        int[] result = new int[2];
        int vSpeed = 0, wSpeed = 0;
        switch (direction) {
            case LEFT: // 往左转
                switch (clockDirection) {
                    case CLOCKWISE:
                        wSpeed = (int) Math.abs((float) angle / duration);
                        vSpeed = -wSpeed * radius;
                        break;
                    case EASTERN:
                        wSpeed = -(int) Math.abs((float) angle / duration);
                        vSpeed = wSpeed * radius;
                        break;
                }
                break;
            case RIGHT: // 往右转
                switch (clockDirection) {
                    case CLOCKWISE:
                        wSpeed = (int) Math.abs((float) angle / duration);
                        vSpeed = wSpeed * radius;
                        break;
                    case EASTERN:
                        wSpeed = -(int) Math.abs((float) angle / duration);
                        vSpeed = -wSpeed * radius;
                        break;
                }
                break;
            default:
                LogUtils.d("setTurn", "Turning direction error...");
        }
        result[0] = vSpeed;
        result[1] = wSpeed;
        return result;
    }

    public static enum Direction {
        FORE, BACK, LEFT, RIGHT
    }

    public static enum ClockDirection {
        CLOCKWISE, EASTERN
    }
}
