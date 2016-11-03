package com.geeknewbee.doraemonsdk.utils;

import android.text.TextUtils;
import android.util.Log;


public class LogUtils {
    public static boolean LOG_DEBUG = false;
    public static boolean LOG_FILE = LOG_DEBUG;

    public static void v(String tag, String msg) {
        if (LOG_DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LOG_DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LOG_DEBUG && !TextUtils.isEmpty(msg)) {
            Log.d(tag, msg);
        }
    }

    public static void printStackTrace() {
        printStackTrace(null);
    }

    public static void printStackTrace(String tag) {
        if (TextUtils.isEmpty(tag)) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        } else {
            v(tag, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }

        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                sb.append(stackElements[i]);
                if (i != stackElements.length - 1) {
                    sb.append("\n\t");
                }
            }
            if (TextUtils.isEmpty(tag)) {
                System.out.println(sb);
            } else {
                v(tag, sb.toString());
            }
        }

        if (TextUtils.isEmpty(tag)) {
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        } else {
            v(tag, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        }

    }

    private static void recLifeCycle(Class<?> callingClass, String note) {
        String className = callingClass.getSimpleName();
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        String methodName = s[4].getMethodName();
    }
}
