
package com.geeknewbee.doraemon.input.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.List;
import java.util.UUID;

public class ImmediateAlertService extends BluetoothGattServerCallback {
    public static final String TAG = "BLE_TAG";
    private final Handler mHandler;
    private BluetoothGattServer mGattServer;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGattCharacteristic read;
    private BluetoothGattCharacteristic notifyTTS;
    private BLEDataReader bleDataReader;
    private boolean hadSetSecret;
    private Runnable secret_runnable;

    public ImmediateAlertService(Handler mHandler) {
        this.mHandler = mHandler;
        bleDataReader = new BLEDataReader();
        secret_runnable = new Runnable() {
            @Override
            public void run() {
                //必须在规定的时间内发送正确的密钥过来否则断开连接
                if (!hadSetSecret && bluetoothDevice != null)
                    mGattServer.cancelConnection(bluetoothDevice);
                LogUtils.d(TAG, " countDownTimer finish:" + hadSetSecret);
            }
        };
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

            BluetoothGattCharacteristic control = new BluetoothGattCharacteristic(
                    UUID.fromString(BleUuid.CHAR_SET_CONTROL_STRING),
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);

            read = new BluetoothGattCharacteristic(
                    UUID.fromString(BleUuid.CHAR_NOTIFY_WIFI_STRING),
                    BluetoothGattCharacteristic.PROPERTY_READ |
                            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            dis.addCharacteristic(mansc);
            dis.addCharacteristic(control);
            dis.addCharacteristic(read);
            mGattServer.addService(dis);

            // 商业场景service
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


            // 密钥service
            BluetoothGattService secret = new BluetoothGattService(
                    UUID.fromString(BleUuid.SERVICE_SECRET_KEY),
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);

            BluetoothGattCharacteristic secretCharacteristic = new BluetoothGattCharacteristic(
                    UUID.fromString(BleUuid.CHAR_SET_SECRET_KEY),
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
            secret.addCharacteristic(secretCharacteristic);
            mGattServer.addService(secret);
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

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status,
                                        int newState) {
        if (newState == BluetoothGattServer.STATE_CONNECTED) {
            bluetoothDevice = device;
            bleDataReader.clearData();
            hadSetSecret = false;

//            mHandler.removeCallbacks(secret_runnable);
//            mHandler.postDelayed(secret_runnable, Constants.BLE_SECRET_OUT_TIME);

            LogUtils.d(TAG, "start countDownTimer");
        } else {
            bluetoothDevice = null;
            bleDataReader.clearData();
        }

        Log.d(TAG, "onConnectionStateChange status=" + status + "->" + newState);
    }

    public void onCharacteristicReadRequest(BluetoothDevice device,
                                            int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);
        switch (characteristic.getUuid().toString()) {
            case BleUuid.CHAR_SET_WIFI_STRING:
            case BleUuid.CHAR_SET_TTS_STRING:
            case BleUuid.CHAR_NOTIFY_WIFI_STRING:
            case BleUuid.CHAR_NOTIFY_TTS_STRING:
                characteristic.setValue("Name:NONE");
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
                break;
        }
    }

    public void onCharacteristicWriteRequest(BluetoothDevice device,
                                             int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                             boolean responseNeeded, int offset, byte[] value) {
        Log.d(TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
                + Boolean.toString(preparedWrite) + " responseNeeded="
                + Boolean.toString(responseNeeded) + " offset=" + offset);
        switch (characteristic.getUuid().toString()) {
            case BleUuid.CHAR_SET_WIFI_STRING:
            case BleUuid.CHAR_SET_CONTROL_STRING:
            case BleUuid.CHAR_SET_TTS_STRING:
//                if (!hadSetSecret)
//                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset,
//                            null);
//                else {
                    receiveCharacteristicData(characteristic, value, Constants.MESSAGE_BLE_CONTROL);
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                            null);
//                }
                break;
            case BleUuid.CHAR_SET_SECRET_KEY:
                if (value == null)
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset,
                            null);
                else {
                    String secret = new String(value);
                    if (secret.equals(Constants.BLE_SECRET)) {
                        hadSetSecret = true;
                        mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset,
                                null);
                    }
                }
                break;
        }
    }

    private void receiveCharacteristicData(BluetoothGattCharacteristic characteristic, byte[] value, int messageWhat) {
        if (value != null && value.length > 0) {
            String readMessage = new String(value, 0, value.length);
            LogUtils.d(TAG, "receive value:" + readMessage);
            String result = bleDataReader.readData(characteristic, readMessage);
            if (!TextUtils.isEmpty(result))
                mHandler.obtainMessage(messageWhat, result.length(), -1, result)
                        .sendToTarget();
        } else {
            LogUtils.d(TAG, "invalid value written");
        }
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
        LogUtils.d(TAG, "onExecuteWrite execute:" + execute);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device, status);
        LogUtils.d(TAG, "onNotificationSent status:" + status);
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
}
