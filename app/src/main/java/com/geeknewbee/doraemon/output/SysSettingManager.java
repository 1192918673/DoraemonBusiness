package com.geeknewbee.doraemon.output;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.WifiCommand;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

/**
 * 系统功能设置
 */
public class SysSettingManager {

    /**
     * 设置系统连接wifi
     *
     * @param type //1 无密码，2 WEB加密, 3 WPA加密
     * @param SSID
     * @param pwd
     */
    public static void connectWiFi(int type, String SSID, String pwd) {
        WifiManager wm = (WifiManager) BaseApplication.mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wfc = new WifiConfiguration();

        wfc.SSID = "\"" + SSID + "\""; // 1.Wifi名
        wfc.status = WifiConfiguration.Status.DISABLED; // 2.Wifi配置状态：disabled
        wfc.priority = 40; // 3.优先级
        wfc.allowedAuthAlgorithms.clear();
        wfc.allowedGroupCiphers.clear();
        wfc.allowedKeyManagement.clear();
        wfc.allowedPairwiseCiphers.clear();
        wfc.allowedProtocols.clear();
        if (type == 1) { //wificipher_nopass
            wfc.wepKeys[0] = "";
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.wepTxKeyIndex = 0;
        }
        if (type == 2) { //wificipher_wep
            wfc.hiddenSSID = true;
            wfc.wepKeys[0] = "\"" + pwd + "\"";
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.wepTxKeyIndex = 0;
        }
        if (type == 3) { //wificipher_wpa
            wfc.preSharedKey = "\"" + pwd + "\"";
            wfc.hiddenSSID = true;
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.status = WifiConfiguration.Status.ENABLED;
        }
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
