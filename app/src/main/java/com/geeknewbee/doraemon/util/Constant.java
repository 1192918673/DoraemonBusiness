package com.geeknewbee.doraemon.util;

public class Constant {
    public static final int RESPONSE_STATUS_SUCCESS = 200;
    public static final int HTTP_TIME_OUT = 30 * 1000;
    public static final String TAG_COMMAND = "ROBOT_COMMAND";
    public static final String TAG_SOUND = "ROBOT_SOUND";
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

}
