package com.geeknewbee.doraemon.output;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

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
        WifiManager wm = (WifiManager) BaseApplication.mContext.getSystemService(Context.WIFI_SERVICE);
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
     * @param volPercent 音量大小百分比，取值范围为 0-100
     */
    public static void setVolume(String volPercent) {
        AudioManager am = (AudioManager) BaseApplication.mContext.getSystemService(Context.AUDIO_SERVICE);
        int streamMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVol = (int) (Integer.parseInt(volPercent) / 100.0 * streamMaxVolume);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        LogUtils.d("MAX", streamMaxVolume + "");
        LogUtils.d("VOLUME", volPercent);
    }
}
