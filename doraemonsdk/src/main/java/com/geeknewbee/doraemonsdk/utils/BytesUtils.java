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
     * int 转为char array
     *
     * @param num
     * @return
     */
    public static char[] int2bytes(int num) {
        char[] b = new char[4];
        int mask = 0xff;
        for (int i = 0; i < 4; i++) {
            b[i] = (char) ((num >>> (24 - i * 8)) & mask);
        }
        return b;
    }

    /**
     * int 转为char array
     * 无符号右移，高位补0
     * 0xff的值为[00000000][00000000][00000000][11111111] 一直保留最后8位不变,其他都为0
     *
     * @param num
     * @return
     */
    public static byte[] int2bytes2(int num) {
        byte[] b = new byte[4];
        int mask = 0xff;
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) ((num >>> (24 - i * 8)) & mask);
        }
        return b;
    }


    /**
     * byte[] 4位转为int
     * 高位在前，低位在后
     *
     * @param bytes
     * @return
     */
    public static int bytes2int(byte[] bytes) {
        int result = 0;
        if (bytes.length == 4) {
            int a = (bytes[0] & 0xff) << 24;
            int b = (bytes[1] & 0xff) << 16;
            int c = (bytes[2] & 0xff) << 8;
            int d = (bytes[3] & 0xff);
            result = a | b | c | d;
        }
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
