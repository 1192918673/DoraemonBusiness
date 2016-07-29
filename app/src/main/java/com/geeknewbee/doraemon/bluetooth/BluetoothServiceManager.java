package com.geeknewbee.doraemon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.control.Doraemon;
import com.geeknewbee.doraemon.model.BluetoothCommand;
import com.geeknewbee.doraemon.task.BluetoothTalkTask;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class BluetoothServiceManager {
    private static volatile BluetoothServiceManager instance;
    private BluetoothAdapter mBluetoothAdapter;
    private Doraemon doraemon;
    private Context context;
    private BluetoothChatService mChatService;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        setupChat();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        if (mChatService != null) {
                            mChatService.stop();
                        }
                        break;
                }
            }
        }
    };
    private BlockingQueue<byte[]> audioData = new LinkedBlockingQueue<byte[]>();
    private BluetoothTalkTask talkTask;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            talkTask.start();
//                            doraemon.addCommand(new Command(CommandType.PLAY_SOUND, "已连接"));
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
//                            doraemon.addCommand(new Command(CommandType.PLAY_SOUND, "连接中"));
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
//                            doraemon.addCommand(new Command(CommandType.PLAY_SOUND, "断开连接"));
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    synchronized (this) {
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        int commandPrefixLength = Constants.COMMAND_ROBOT_PREFIX.length();
                        String readMessage = new String(readBuf, 0, commandPrefixLength);
                        if (readMessage.equals(Constants.COMMAND_ROBOT_PREFIX)) {
                            //robot command
                            Gson gson = new Gson();
                            try {
                                readMessage = new String(readBuf, commandPrefixLength, readBuf.length - commandPrefixLength);
                                BluetoothCommand command = gson.fromJson(readMessage, BluetoothCommand.class);
                                doraemon.addCommand(command.getCommand());
                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
                            }
                        } else {
                            //播放声音
                            audioData.add(readBuf);
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
            }
        }
    };

    private BluetoothServiceManager(Context context) {
        this.context = context;
    }

    public static BluetoothServiceManager getInstance(Context context) {
        if (instance == null) {
            synchronized (BluetoothServiceManager.class) {
                if (instance == null) {
                    instance = new BluetoothServiceManager(context);
                }
            }
        }
        return instance;
    }

    public void onCreate() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);
        doraemon = Doraemon.getInstance(context);
        talkTask = new BluetoothTalkTask(audioData);
    }

    public void onStart() {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    public void onResume() {
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    public void onDestroy() {
        context.unregisterReceiver(mReceiver);
        if (mChatService != null) {
            mChatService.stop();
        }

        if (talkTask != null) {
            talkTask.stop();
        }
    }

    private void setupChat() {
        if (mChatService == null)
            mChatService = new BluetoothChatService(context, mHandler);
        if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
            // Start the Bluetooth chat services
            mChatService.start();
        }
    }

}
