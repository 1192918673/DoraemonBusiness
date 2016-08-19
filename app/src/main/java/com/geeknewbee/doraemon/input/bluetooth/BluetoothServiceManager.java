package com.geeknewbee.doraemon.input.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.output.BluetoothTalkTask;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.command.BluetoothCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
                        setupBluetoothServer();
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
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ_SOUND:
                    byte[] readBuf = (byte[]) msg.obj;
                    //播放声音
                    audioData.add(readBuf);
                    break;
                case Constants.MESSAGE_READ_COMMAND:
                    byte[] buf = (byte[]) msg.obj;
                    Gson gson = new Gson();
                    try {
                        String readMessage = new String(buf, 0, buf.length);
                        BluetoothCommand command = gson.fromJson(readMessage, BluetoothCommand.class);
                        doraemon.addCommand(command.getCommand());
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    private BluetoothGattServer mGattServer;
    private BluetoothLeAdvertiser mBTAdvertiser;
    private ImmediateAlertService ias;

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

    public void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, filter);
        doraemon = Doraemon.getInstance(context);
        talkTask = new BluetoothTalkTask(audioData);
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.setName(Constants.BLUETOOTH_NAME);
    }

    public void start() {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) {
                setupBluetoothServer();
            }
            startAdvertise();
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

        stopAdvertise();
    }

    /**
     * 开启蓝牙2.0 sever
     */
    private void setupBluetoothServer() {
        if (mChatService == null)
            mChatService = new BluetoothChatService(context, mHandler);
        if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
            // Start the Bluetooth chat services
            mChatService.start();
        }
    }

    /**
     * 开启BLE 外围设备模式
     */
    private void startAdvertise() {
        if (mBluetoothAdapter == null) {
            return;
        }

        if (!BleUtil.isBLESupported(context) || !mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            LogUtils.d(ImmediateAlertService.TAG, "is not support ble");
            return;
        }

        if (mBTAdvertiser == null) {
            mBTAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        }
        if (mBTAdvertiser != null) {
            ias = new ImmediateAlertService();
            mGattServer = BleUtil.getManager(context).openGattServer(context, ias);
            ias.setupServices(mGattServer);

            mBTAdvertiser.startAdvertising(
                    BleUtil.createAdvSettings(true, 0),
                    BleUtil.createFMPAdvertiseData(),
                    mAdvCallback);
        }
    }

    private void stopAdvertise() {
        if (mGattServer != null) {
            mGattServer.clearServices();
            mGattServer.close();
            mGattServer = null;
        }
        if (mBTAdvertiser != null) {
            mBTAdvertiser.stopAdvertising(mAdvCallback);
            mBTAdvertiser = null;
        }
    }

    private AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        public void onStartSuccess(android.bluetooth.le.AdvertiseSettings settingsInEffect) {
            if (settingsInEffect != null) {
                LogUtils.d(ImmediateAlertService.TAG, "onStartSuccess TxPowerLv="
                        + settingsInEffect.getTxPowerLevel()
                        + " mode=" + settingsInEffect.getMode()
                        + " timeout=" + settingsInEffect.getTimeout());
            } else {
                LogUtils.d(ImmediateAlertService.TAG, "onStartSuccess, settingInEffect is null");
            }
        }

        public void onStartFailure(int errorCode) {
            LogUtils.d(ImmediateAlertService.TAG, "onStartFailure errorCode=" + errorCode);
        }
    };
}
