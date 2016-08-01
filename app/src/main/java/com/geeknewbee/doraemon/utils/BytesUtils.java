package com.geeknewbee.doraemon.utils;

public class BytesUtils {
    /**
     * 通过byte数组取到short
     *
     * @param b
     * @return
     */
    public static short[] getShort(byte[] b) {
        if (b == null) {
            return null;
        }
        short[] s = new short[b.length / 2];
        for (int i = 0; i < s.length; i++) {
            s[i] = (short) (((b[i * 2 + 1] << 8) | b[i * 2 + 0] & 0xff));
        }
        return s;
    }

    /**
     * 根据int 获取高，低 两个char
     *
     * @param value
     * @return
     */
    public static char[] getHighAndLowChar(int value) {
        char[] result = new char[2];

        //高8位
        result[0] = (char) ((value >> 8) & 0xff);
        //低8位
        result[1] = (char) (value & 0xff);
        return result;
    }

    /**
     * 根据 高，低 char 获取int value
     *
     * @param highChar
     * @param lowChar
     * @return
     */
    public static int getIntValueByHighAndLowChar(char highChar, char lowChar) {
        int high = highChar;
        int low = lowChar;
        return (high << 8) + low;
    }
}
