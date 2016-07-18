package com.sangebaba.doraemon.business.utils;

import android.util.Log;

/**
 * Created by ACER on 2016/7/18.
 */
public class Loger {

    public static boolean showLog = true;

    /**
     * 1.打印调试信息方法
     *
     * @param objTag 如果这个对象是String，则直接使用，否则使用这个对象的类名
     * @param objMsg 使用这个对象的toString()方法作为Log信息
     */
    public static void d(Object objTag, Object objMsg) {
        if (!showLog) {
            return;
        }

        String tag;
        if (objTag instanceof String) {
            tag = (String) objTag;
        } else if (objTag instanceof Class) {
            tag = ((Class) objTag).getSimpleName();
        } else {
            tag = objTag.getClass().getSimpleName();
        }

        String msg = (objMsg == null || objMsg.toString() == null) ? "null" : objMsg.toString();

        Log.d(tag, msg);
    }
}
