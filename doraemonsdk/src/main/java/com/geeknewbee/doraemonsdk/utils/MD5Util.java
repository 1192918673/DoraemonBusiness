package com.geeknewbee.doraemonsdk.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String encrypt(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
    
    /**
     * md5加密
     *
     * @param psw
     * @return
     */
    public static String md5(String psw) {
        StringBuilder sb = new StringBuilder();
        try {
            //数据摘要器
            //algorithm : 加密方式
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            //第一次加密，将一个byte数组进行加密，返回一个加密过的byte数据，二进制哈希算法
            byte[] digest = messageDigest.digest(psw.getBytes());
            for (int i = 0; i < digest.length; i++) {
                //byte:-128--127
                //将负数转化成正整数
                int reuslt = digest[i] & 0xff;
                //将int类型的数据转化成十六进制的字符串
                //String hexString = Integer.toHexString(reuslt)+1;//不规则加密，加盐操作
                String hexString = Integer.toHexString(reuslt);
                if (hexString.length() < 2) {
                    //System.out.print("0");
                    sb.append("0");
                }
                //System.out.println(hexString);
                sb.append(hexString);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return psw;
    }
}
