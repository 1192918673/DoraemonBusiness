package com.geeknewbee.doraemon.input.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * BLE数据发送对象
 */
public class BLEDataSender {
    public static final int MAX_LENGTH = 18;
    private Map<BluetoothGattCharacteristic, Queue<byte[]>> characteristicQueueMap;
    private BluetoothGattServer gattServer;
    private BluetoothDevice bluetoothDevice;

    public void init(BluetoothDevice bluetoothDevice, BluetoothGattServer gattServer) {
        characteristicQueueMap = new HashMap<>();
        this.gattServer = gattServer;
        this.bluetoothDevice = bluetoothDevice;
    }

    /**
     * 获取分包
     *
     * @param data
     * @return
     */
    public static List<byte[]> getDataArray(String data) {
        if (TextUtils.isEmpty(data))
            return null;

        data = Constants.COMMAND_ROBOT_PREFIX + data + Constants.COMMAND_ROBOT_SUFFIX;

        List<byte[]> result = new ArrayList<>();
        byte[] bytes = data.getBytes();
        int length = bytes.length;
        int number = length % MAX_LENGTH == 0 ? length / MAX_LENGTH : length / MAX_LENGTH + 1;
        for (int i = 0; i < number; i++) {
            byte[] range = Arrays.copyOfRange(bytes, i * 18, i == number - 1 ? length : (i + 1) * MAX_LENGTH);
            result.add(range);
        }

        return result;
    }

    public synchronized void addData(BluetoothGattCharacteristic characteristic, String data) {
        if (TextUtils.isEmpty(data))
            return;

        Queue<byte[]> queue = characteristicQueueMap.get(characteristic);
        if (queue == null)
            queue = new ArrayDeque<>();

        //当前队列是空的时候，在添加完队列后需要发送一次数据。否则由onCharacteristicWrite 触发发送
        boolean needSendDataAfterAddData = queue.size() == 0;
        //分包
        byte[] bytes = data.getBytes();
        int length = bytes.length;
        int number = length % MAX_LENGTH == 0 ? length / MAX_LENGTH : length / MAX_LENGTH + 1;
        for (int i = 0; i < number; i++) {
            byte[] range = Arrays.copyOfRange(bytes, i * 18, i == number - 1 ? length : (i + 1) * MAX_LENGTH);
            queue.offer(range);
        }

        if (!characteristicQueueMap.containsKey(characteristic)) {
            characteristicQueueMap.put(characteristic, queue);
        }

        if (needSendDataAfterAddData)
            sendNextPackage(characteristic);
    }

    public synchronized void sendNextPackage(BluetoothGattCharacteristic characteristic) {
        if (gattServer == null || bluetoothDevice == null)
            return;

        Queue<byte[]> queue = characteristicQueueMap.get(characteristic);
        byte[] bytes = queue.poll();
        if (bytes == null)
            return;
        characteristic.setValue(bytes);
        gattServer.notifyCharacteristicChanged(bluetoothDevice, characteristic, false);//测试发现必须发送此通知,并且保证特征值的notify权限
    }

    public synchronized void clearAllData() {
        gattServer = null;
        bluetoothDevice = null;
        if (characteristicQueueMap != null)
            characteristicQueueMap.clear();
    }
}
