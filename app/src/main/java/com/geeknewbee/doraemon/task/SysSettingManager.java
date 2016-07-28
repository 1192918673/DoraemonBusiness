package com.geeknewbee.doraemon.task;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.util.LogUtils;

/**
 * Created by ACER on 2016/7/28.
 */
public class SysSettingManager {

    private static WifiManager wm;
    private static WifiConfiguration wc;
    private static AudioManager am;

    /**
     * 设置系统连接wifi
     *
     * @param content
     */
    public static void connectWiFi(String content) {
        wm = (WifiManager) App.mContext.getSystemService(Context.WIFI_SERVICE);
        wc = new WifiConfiguration();
        String[] data = content.split("#");
        /*----------------------WPA连接方式------------------------*/
        wc.SSID = "\" " + data[0] + "\"";
        LogUtils.d("SSID", wc.SSID);
        wc.hiddenSSID = false;

        wc.status = WifiConfiguration.Status.ENABLED;


        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

        wc.preSharedKey = "\" " + data[1] + "\"";
        LogUtils.d("PWD", wc.preSharedKey);
        int res = wm.addNetwork(wc);

        boolean b = wm.enableNetwork(res, false);
        /*----------------------WPA连接方式END------------------------*/
        LogUtils.d("WIFI:", +res + "\n" + b + "");
    }

    /**
     * 设置系统音量
     *
     * @param content
     */
    public static void setVolume(String content) {
        am = (AudioManager) App.mContext.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_SYSTEM, Integer.parseInt(content), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        LogUtils.d("VOLUME", content);
    }
}
