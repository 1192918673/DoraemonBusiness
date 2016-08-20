package com.geeknewbee.doraemonsdk.utils;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FilenameFilter;

public class DeviceUtil {
    /**
     * 获取 i/o dev 的名字。usb设备在热插拔的时候前面的名字不会变，后缀会变X(0.1.2)。例如乐行的设备
     *
     * @param devicePrefix
     * @return
     */
    public static String getIODeviceName(final String devicePrefix) {
        String result = null;
        try {
            File is = new File("/dev");
            if (!is.canRead())
                LogUtils.d("LeXingFoot", "can not read dev folder");

            String[] strings = is.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(devicePrefix);
                }
            });
            if (strings != null && strings.length > 0)
                result = strings[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取MAC地址
     *
     * @return
     */
    public static String getWifiMAC(Context context) {
        String macAddress = "000000000000";
        try {
            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
            if (null != info) {
                if (!TextUtils.isEmpty(info.getMacAddress()))
                    macAddress = info.getMacAddress();
                else
                    return macAddress;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return macAddress;
        }
        return macAddress;
    }

    /**
     * 获取App version name
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String versionName = info.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
