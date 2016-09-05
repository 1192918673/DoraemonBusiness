/*
 * Copyright (C) 2013 youten
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

/**
 * BLE UUID Strings
 */
public class BleUuid {
    // 180A Device Information
    public static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SERIAL_NUMBER_STRING = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SET_WIFI_STRING = "00002a30-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_NOTIFY_WIFI_STRING = "00002a31-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_NOTIFY_TTS_STRING = "00002a32-0000-1000-8000-00805f9b34fb";

    // 1802 Immediate Alert
    public static final String SERVICE_IMMEDIATE_ALERT = "00001802-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_ALERT_LEVEL = "00002a06-0000-1000-8000-00805f9b34fb";

    // 180F Battery
    public static final String SERVICE_BATTERY_SERVICE = "0000180F-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";

    // 180D Heart Rate
    public static final String SERVICE_HEART_RATE = "0000180d-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_BATTER = "00002a19-0000-1000-8000-00805f9b34fb";

}
