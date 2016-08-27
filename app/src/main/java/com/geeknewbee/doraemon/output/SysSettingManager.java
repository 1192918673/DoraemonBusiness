package com.geeknewbee.doraemon.output;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.geeknewbee.doraemon.entity.event.SetWifiCompleteEvent;
import com.geeknewbee.doraemon.processcenter.ShowQRTask;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * 系统功能设置
 */
public class SysSettingManager {

    public static final String TAG = SysSettingManager.class.getSimpleName();

    public static boolean connectWiFi(String ssid, String pwd, int type) {
        boolean result = false;
        WifiManager wm = (WifiManager) BaseApplication.mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"".concat(ssid).concat("\"");
        config.status = WifiConfiguration.Status.DISABLED;
        config.priority = 40;
        if (type == 1) {
            //NO PWD
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.clear();
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        } else if (type == 2) {
            //wep
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            if (pwd.matches("^[0-9a-fA-F]+$")) {
                config.wepKeys[0] = pwd;
            } else {
                config.wepKeys[0] = "\"".concat(pwd).concat("\"");
            }
            config.wepTxKeyIndex = 0;
        } else if (type == 3) {
            //WPA连接方式
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.preSharedKey = "\"".concat(pwd).concat("\"");
        }
        LogUtils.d("PWD", config.preSharedKey);
        int res = wm.addNetwork(config);

        if (res != -1) {
            result = wm.enableNetwork(res, true);
            if (result) new ShowQRTask().start();
        }

        EventBus.getDefault().post(new SetWifiCompleteEvent(result, ssid));
        return result;
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
        LogUtils.d(TAG, "VOLUME：" + volPercent);
        LogUtils.d(TAG, "CURRENTVOL：" + currentVol);
        LogUtils.d(TAG, "MAX：" + streamMaxVolume);
    }
}
