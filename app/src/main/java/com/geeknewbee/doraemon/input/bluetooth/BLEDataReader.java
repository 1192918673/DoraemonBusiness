package com.geeknewbee.doraemon.input.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * BLE BluetoothGattCharacteristic 分包读取
 */
public class BLEDataReader {
    private Map<BluetoothGattCharacteristic, String> dataMap;

    public BLEDataReader() {
        dataMap = new HashMap<>();
    }

    /**
     * 分包读取数据，当时一个完整的命令的时候，直接返回命令字符串，否则返回EMPTY
     *
     * @param characteristic
     * @param value
     * @return
     */
    public synchronized String readData(BluetoothGattCharacteristic characteristic, String value) {
        if (TextUtils.isEmpty(value))
            return Constants.EMPTY_STRING;

        String oldData;
        if (dataMap.containsKey(characteristic))
            oldData = dataMap.get(characteristic);
        else
            oldData = Constants.EMPTY_STRING;

        oldData += value;

        dataMap.put(characteristic, oldData);

        if (oldData.startsWith(Constants.COMMAND_ROBOT_PREFIX)
                && oldData.endsWith(Constants.COMMAND_ROBOT_SUFFIX)) {
            dataMap.put(characteristic, Constants.EMPTY_STRING); //当结束一个完整的命令的时候需要 清空Map中的值
            return oldData.substring(Constants.COMMAND_ROBOT_PREFIX.length(), oldData.length() - Constants.COMMAND_ROBOT_SUFFIX.length());
        } else
            return Constants.EMPTY_STRING;
    }

    public synchronized void clearData() {
        dataMap.clear();
    }
}
