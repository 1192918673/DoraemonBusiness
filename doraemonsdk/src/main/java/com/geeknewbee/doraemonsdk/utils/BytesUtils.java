package com.geeknewbee.doraemonsdk.utils;

import java.util.Arrays;

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

    public static byte[] concat(byte[] first, byte[] second) {
        if (first == null) return second;
        else if (second == null) return first;
        else {
            byte[] result = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }
    }
}
