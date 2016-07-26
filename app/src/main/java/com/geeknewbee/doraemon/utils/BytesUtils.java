package com.geeknewbee.doraemon.utils;

public class BytesUtils {
    /**
     * 通过byte数组取到short
     *
     * @param b
     * @param index 第几位开始取
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
}
