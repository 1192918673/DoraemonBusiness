package com.geeknewbee.doraemon.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

    private static SharedPreferences sp;

    /**
     * 保存boolean值信息
     */
    public static void saveBoolean(Context context, String key, boolean value) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        sp.edit().putBoolean(key, value).commit();
    }

    /**
     * 获取boolean值的信息
     *
     * @return
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key, defValue);
    }

    /**
     * 保存boolean值信息
     */
    public static void saveFloat(Context context, String key, float value) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        sp.edit().putFloat(key, value).commit();
    }

    /**
     * 获取boolean值的信息
     *
     * @return
     */
    public static float getFloat(Context context, String key, float defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return sp.getFloat(key, defValue);
    }

    /**
     * 保存String值信息
     */
    public static void saveString(Context context, String key, String value) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        sp.edit().putString(key, value).commit();
    }

    /**
     * 获取String值的信息
     *
     * @return
     */
    public static String getString(Context context, String key, String defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return sp.getString(key, defValue);
    }

    /**
     * 保存int值信息
     */
    public static void saveInt(Context context, String key, int value) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        sp.edit().putInt(key, value).commit();
    }

    /**
     * 获取int值信息
     *
     * @return
     */
    public static int getInt(Context context, String key, int defValue) {
        if (sp == null) {
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return sp.getInt(key, defValue);
    }
}
