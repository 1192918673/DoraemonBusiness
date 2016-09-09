
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
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.UUID;

public class ImmediateAlertService extends BluetoothGattServerCallback {
    public static final String TAG = "BLE_TAG";
    private final Handler mHandler;

    private BluetoothGattServer mGattServer;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGattCharacteristic read;
    private BluetoothGattCharacteristic notifyTTS;

    public ImmediateAlertService(Handler mHandler) {
        this.mHandler = mHandler;
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
        if (newState == BluetoothGattServer.STATE_CONNECTED)
            bluetoothDevice = device;
        else
            bluetoothDevice = null;

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
            LogUtils.d(TAG, "CHAR_ALERT_LEVEL");
            if (value != null && value.length > 0) {
                mHandler.obtainMessage(Constants.MESSAGE_BLE_WIFI, value.length, -1, value)
                        .sendToTarget();
            } else {
                LogUtils.d(TAG, "invalid value written");
            }
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

    //主动写值并通知远程设备wifi
    public void sendWifiNotification(String value) {
        if (bluetoothDevice != null && mGattServer != null) {
            read.setValue(value);
            mGattServer.notifyCharacteristicChanged(bluetoothDevice, read, false);//测试发现必须发送此通知,并且保证特征值的notify权限
        }
    }

    public void sendTTSNotification(String value) {
        if (bluetoothDevice != null && mGattServer != null) {
            notifyTTS.setValue(value);
            mGattServer.notifyCharacteristicChanged(bluetoothDevice, notifyTTS, false);//测试发现必须发送此通知,并且保证特征值的notify权限
        }
    }

}
