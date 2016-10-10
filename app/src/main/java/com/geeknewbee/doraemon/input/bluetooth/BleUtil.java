/*
 * Copyright (C) 2014 youten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.geeknewbee.doraemon.input.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;

import java.util.UUID;

/**
 * Util for Bluetooth Low Energy
 */
public class BleUtil {

    private BleUtil() {
        // Util
    }

    /**
     * check if BLE Supported device
     */
    public static boolean isBLESupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * get BluetoothManager
     */
    public static BluetoothManager getManager(Context context) {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    /**
     * create AdvertiseSettings
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        // ConnectableをtrueにするとFlags AD typeの3byteがManufacturer specific data等の前につくようになります。
        builder.setConnectable(connectable);
        builder.setTimeout(timeoutMillis);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        return builder.build();
    }

    /**
     * create AdvertiseDate for FMP(Find Me Profile, include IAS and DIS)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseData createFMPAdvertiseData() {
        AdvertiseData.Builder builder = new AdvertiseData.Builder();

        builder.setIncludeDeviceName(true);
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(BleUuid.SERVICE_DEVICE_INFORMATION)));
        builder.addServiceUuid(new ParcelUuid(UUID.fromString(BleUuid.SERVICE_BUSINESS)));
        AdvertiseData adv = builder.build();
        return adv;
    }

}
