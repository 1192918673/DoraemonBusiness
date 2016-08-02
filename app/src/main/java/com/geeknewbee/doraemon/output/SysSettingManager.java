package com.geeknewbee.doraemon.output;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.utils.LogUtils;

/**
 * 系统功能设置
 */
public class SysSettingManager {

    /**
     * 设置系统连接wifi
     *
     * @param content
     */
    public static void connectWiFi(String content) {
        WifiManager wm = (WifiManager) App.mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wfc = new WifiConfiguration();
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
        AudioManager am = (AudioManager) App.mContext.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(content), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        int streamMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        LogUtils.d("MAX", streamMaxVolume + "");
        LogUtils.d("VOLUME", content);
    }
}
