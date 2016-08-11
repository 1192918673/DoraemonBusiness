package com.geeknewbee.doraemonsdk.utils;

import com.geeknewbee.doraemonsdk.output.action.LeXingFoot;

import java.io.File;
import java.io.FilenameFilter;

public class DeviceUtil {
    /**
     * 获取dev 的名字。usb设备在热插拔的时候前面的名字不会变，后面会是X(0.1.2)。例如乐行的设备
     *
     * @param devicePrefix
     * @return
     */
    public static String getDeviceName(final String devicePrefix) {
        String result = null;
        try {
            File is = new File("/dev");
            if (!is.canRead())
                LogUtils.d(LeXingFoot.TAG, "can not read dev folder");

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
}