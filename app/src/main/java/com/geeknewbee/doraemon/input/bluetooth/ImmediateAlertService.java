
package com.geeknewbee.doraemon.input.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.util.Log;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemonsdk.utils.BytesUtils;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ImmediateAlertService extends BluetoothGattServerCallback {
    public static final String TAG = "BLE_TAG";
    private final Handler mHandler;
    private final int prefixLength;
    private final int suffixLength;
    private BluetoothGattServer mGattServer;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGattCharacteristic read;
    private BluetoothGattCharacteristic notifyTTS;
    private boolean isReceivingWifiCommand;
    private byte[] setWifiResult;
    //创建一个切换setWifiLock锁对象
    private Lock setWifiLock = new ReentrantLock();
    private BLEDataSender bleDataSender;
    private BluetoothGattCharacteristic lastCharacteristic;

    public ImmediateAlertService(Handler mHandler) {
        this.mHandler = mHandler;
        prefixLength = Constants.COMMAND_ROBOT_PREFIX.length();
        suffixLength = Constants.COMMAND_ROBOT_SUFFIX.length();
        //是否正在接收WIFI定义的命令
        isReceivingWifiCommand = false;
        setWifiResult = null;
        bleDataSender = new BLEDataSender();
    }

    public void setupServices(BluetoothGattServer gattServer) {
        if (gattServer == null) {
            throw new IllegalArgumentException("gattServer is null");
        }
        mGattServer = gattServer;

        // setup services
        {
            // doraemon information
            BluetoothGattService dis = new BluetoothGattService(
                    UUID.fromString(BleUuid.SERVICE_DEVICE_INFORMATION),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);
            // manufacturer name string char.
            BluetoothGattCharacteristic mansc = new BluetoothGattCharacteristic(
                    UUID.fromString(BleUuid.CHAR_SET_WIFI_STRING),
                    BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_WRITE |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ |
                            BluetoothGattCharacteristic.PERMISSION_WRITE);


            read = new BluetoothGattCharacteristic(
                    UUID.fromString(BleUuid.CHAR_NOTIFY_WIFI_STRING),
                    BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            dis.addCharacteristic(mansc);
            dis.addCharacteristic(read);
            mGattServer.addService(dis);

            // business information
            BluetoothGattService business = new BluetoothGattService(
                    UUID.fromString(BleUuid.SERVICE_BUSINESS),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);

            BluetoothGattCharacteristic tts = new BluetoothGattCharacteristic(
                    UUID.fromString(BleUuid.CHAR_SET_TTS_STRING),
                    BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_WRITE |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ |
                            BluetoothGattCharacteristic.PERMISSION_WRITE);

            notifyTTS = new BluetoothGattCharacteristic(
                    UUID.fromString(BleUuid.CHAR_NOTIFY_TTS_STRING),
                    BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);

            business.addCharacteristic(tts);
            business.addCharacteristic(notifyTTS);
            mGattServer.addService(business);

        }
    }

    public void onServiceAdded(int status, BluetoothGattService service) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onServiceAdded status=GATT_SUCCESS service="
                    + service.getUuid().toString());
        } else {
            Log.d(TAG, "onServiceAdded status!=GATT_SUCCESS");
        }
    }

    public void onConnectionStateChange(BluetoothDevice device, int status,
                                        int newState) {
        if (newState == BluetoothGattServer.STATE_CONNECTED) {
            bluetoothDevice = device;
            bleDataSender.init(bluetoothDevice, mGattServer);
        } else {
            bluetoothDevice = null;
            bleDataSender.clearAllData();
        }

        Log.d(TAG, "onConnectionStateChange status=" + status + "->" + newState);
    }

    public void onCharacteristicReadRequest(BluetoothDevice device,
                                            int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);
        if (characteristic.getUuid().equals(
                UUID.fromString(BleUuid.CHAR_SET_WIFI_STRING))) {
            Log.d(TAG, "CHAR_SET_WIFI_STRING");
            characteristic.setValue("Name:WIFI");
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    characteristic.getValue());
        } else if (characteristic.getUuid().equals(
                UUID.fromString(BleUuid.CHAR_SET_TTS_STRING))) {
            characteristic.setValue("Name:TTS");
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    characteristic.getValue());
        } else if (characteristic.getUuid().equals(
                UUID.fromString(BleUuid.CHAR_NOTIFY_WIFI_STRING))) {
            characteristic.setValue("Name:NOTIFY WIFI");
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    characteristic.getValue());
        } else if (characteristic.getUuid().equals(
                UUID.fromString(BleUuid.CHAR_NOTIFY_TTS_STRING))) {
            characteristic.setValue("Name:NOTIFY TTS");
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    characteristic.getValue());
        }

    }

    public void onCharacteristicWriteRequest(BluetoothDevice device,
                                             int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                             boolean responseNeeded, int offset, byte[] value) {
        Log.d(TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
                + Boolean.toString(preparedWrite) + " responseNeeded="
                + Boolean.toString(responseNeeded) + " offset=" + offset);
        if (characteristic.getUuid().equals(
                UUID.fromString(BleUuid.CHAR_SET_WIFI_STRING))) {
            setWifiLock.lock();

            if (value != null && value.length > 0) {
                String readMessage = new String(value, 0, value.length);
                LogUtils.d(TAG, "set wifi value:" + readMessage);

                if (isReceivingWifiCommand) {
                    //还在接收Command 的后续包
                    setWifiResult = BytesUtils.concat(setWifiResult, value);

                    isReceivingWifiCommand = !checkIsEndOfCommand(setWifiResult, suffixLength, Constants.MESSAGE_BLE_WIFI);
                } else {
                    if (value.length > prefixLength) {
                        String prefix = new String(value, 0, prefixLength);
                        if (prefix.equals(Constants.COMMAND_ROBOT_PREFIX)) {
                            //是命令的第一个包
                            setWifiResult = value;
                            isReceivingWifiCommand = !checkIsEndOfCommand(setWifiResult, suffixLength, Constants.MESSAGE_BLE_WIFI);
                        }
                    }
                }
            } else {
                LogUtils.d(TAG, "invalid value written");
            }
            setWifiLock.unlock();
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    null);
        } else if (characteristic.getUuid().equals(
                UUID.fromString(BleUuid.CHAR_SET_TTS_STRING))) {
            LogUtils.d(TAG, "Get TTS string:" + value.length);
            if (value.length > 0) {
                mHandler.obtainMessage(Constants.MESSAGE_BLE_TTS, value.length, -1, value)
                        .sendToTarget();
            }

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    null);
        }
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device, status);
        LogUtils.d(TAG, "onNotificationSent status:" + status);
//        if (status == BluetoothGatt.GATT_SUCCESS) {
//            bleDataSender.sendNextPackage(lastCharacteristic);
//        }
    }

    //主动写值并通知远程设备wifi
    public void sendWifiNotification(String value) {
        if (bluetoothDevice != null && mGattServer != null) {
            LogUtils.d(TAG, "notification WIFI :" + value);
            List<byte[]> list = BLEDataSender.getDataArray(value);
            if (list == null) return;
            for (byte[] bytes : list) {
                read.setValue(bytes);
                mGattServer.notifyCharacteristicChanged(bluetoothDevice, read, false);//测试发现必须发送此通知,并且保证特征值的notify权限
                lastCharacteristic = read;
            }
        }
    }

    /**
     * 发送个IOS 告知TTS完成
     *
     * @param value
     */
    public void sendTTSNotification(String value) {
        if (bluetoothDevice != null && mGattServer != null) {
            LogUtils.d(TAG, "notification TTS :" + value);
            notifyTTS.setValue(value);
            mGattServer.notifyCharacteristicChanged(bluetoothDevice, notifyTTS, false);//测试发现必须发送此通知,并且保证特征值的notify权限
        }
    }


    /**
     * 是否完成一个command
     *
     * @param result
     * @param suffixLength
     * @param readCommand
     * @return
     */
    private boolean checkIsEndOfCommand(byte[] result, int suffixLength, int readCommand) {
        String suffix = new String(result, result.length - suffixLength, suffixLength);
        if (suffix.equals(Constants.COMMAND_ROBOT_SUFFIX)) {
            //如果检测到命令完成标志 则sendMessage
            int length = result.length - Constants.COMMAND_ROBOT_SUFFIX.length() - Constants.COMMAND_ROBOT_PREFIX.length();
            byte[] command = new byte[length];
            System.arraycopy(result, Constants.COMMAND_ROBOT_PREFIX.length(), command, 0, length);
            mHandler.obtainMessage(readCommand, command.length, -1, command)
                    .sendToTarget();
            return true;
        }
        return false;
    }
}
