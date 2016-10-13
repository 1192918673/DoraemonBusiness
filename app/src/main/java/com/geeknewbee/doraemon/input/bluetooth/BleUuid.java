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
    // 180A Doraemon Information
    public static final String SERVICE_DEVICE_INFORMATION = "0000180a-0000-1000-8000-00805f9b34fb";

    public static final String CHAR_SET_WIFI_STRING = "00002a30-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_NOTIFY_WIFI_STRING = "00002a31-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SET_CONTROL_STRING = "00002a32-0000-1000-8000-00805f9b34fb";

    // 180B Business
    public static final String SERVICE_BUSINESS = "0000180b-0000-1000-8000-00805f9b34fb";

    public static final String CHAR_SET_TTS_STRING = "00002a60-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_NOTIFY_TTS_STRING = "00002a61-0000-1000-8000-00805f9b34fb";

    //secret key
    public static final String SERVICE_SECRET_KEY = "0000180c-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_SET_SECRET_KEY = "00002a40-0000-1000-8000-00805f9b34fb";
}
