package com.geeknewbee.doraemon.input.wireless;

import android.bluetooth.BluetoothGattCharacteristic;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemonsdk.utils.BytesUtils;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * BLE BluetoothGattCharacteristic 分包读取
 */
public class BLEDataReader {
    private Map<BluetoothGattCharacteristic, byte[]> dataMap;

    public BLEDataReader() {
        dataMap = new HashMap<>();
    }

    public synchronized String readData(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (value == null)
            return Constants.EMPTY_STRING;

        byte[] oldData;
        if (dataMap.containsKey(characteristic))
            oldData = dataMap.get(characteristic);
        else
            oldData = null;

        oldData = BytesUtils.concat(oldData, value);

        dataMap.put(characteristic, oldData);

        String prefix = new String(oldData, 0, Constants.COMMAND_ROBOT_PREFIX.length());
        String suffix = new String(oldData, oldData.length - Constants.COMMAND_ROBOT_SUFFIX.length(), Constants.COMMAND_ROBOT_SUFFIX.length());

        if (prefix.equals(Constants.COMMAND_ROBOT_PREFIX)
                && suffix.equals(Constants.COMMAND_ROBOT_SUFFIX)) {
            dataMap.put(characteristic, null); //当结束一个完整的命令的时候需要 清空Map中的值
            String result = null;
            try {
                result = new String(oldData, Constants.COMMAND_ROBOT_PREFIX.length(),
                        oldData.length - Constants.COMMAND_ROBOT_PREFIX.length() - Constants.COMMAND_ROBOT_SUFFIX.length(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            LogUtils.d(ImmediateAlertService.TAG, "completion :" + result);
            return result;
        } else if (suffix.equals(Constants.COMMAND_ROBOT_SUFFIX)) {
            dataMap.put(characteristic, null); //当一个命令结束了，但是是不完整的命令，则抛弃掉
            return Constants.EMPTY_STRING;
        } else
            return Constants.EMPTY_STRING;
    }

    public synchronized void clearData() {
        dataMap.clear();
    }
}
