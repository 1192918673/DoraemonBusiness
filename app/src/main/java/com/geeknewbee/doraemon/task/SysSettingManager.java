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
    private static WifiConfiguration wfc;
    private static AudioManager am;

    /**
     * 设置系统连接wifi
     *
     * @param content
     */
    public static void connectWiFi(String content) {
        wm = (WifiManager) App.mContext.getSystemService(Context.WIFI_SERVICE);
        wfc = new WifiConfiguration();
        String[] data = content.split("#");
        /*----------------------WPA连接方式------------------------*/
        wfc.SSID = "\"".concat(data[0]).concat("\"");
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 40;

        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        wfc.preSharedKey = "\"".concat(data[1]).concat("\"");

        LogUtils.d("PWD", wfc.preSharedKey);
        int res = wm.addNetwork(wfc);

        if (res != -1) {
            wm.enableNetwork(res, true);
        }
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
