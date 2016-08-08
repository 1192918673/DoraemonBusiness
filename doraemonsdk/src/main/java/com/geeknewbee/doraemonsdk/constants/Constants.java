package com.geeknewbee.doraemonsdk.constants;

public class Constants {
    public static final int RESPONSE_STATUS_SUCCESS = 200;
    public static final int HTTP_TIME_OUT = 30 * 1000;
    public static final String TAG_COMMAND = "ROBOT_COMMAND";
    public static final String TAG_SOUND = "ROBOT_SOUND";
    public static final String TAG_MUSIC = "ROBOT_MUSIC";
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_READ_SOUND = 6;
    public static final int MESSAGE_READ_COMMAND = 7;


    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final String DEFAULT_GIF = "default_gif";
    public static final String STOP_FLAG = "停";
    public static final String COMMAND_ROBOT_PREFIX = "COMMAND_ROBOT";
    public static final String COMMAND_ROBOT_SUFFIX = "COMMAND_ROBOT_SUFFIX";
}
