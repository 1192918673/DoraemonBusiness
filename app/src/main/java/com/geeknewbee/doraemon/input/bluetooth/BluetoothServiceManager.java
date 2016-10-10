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

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.event.SetWifiCompleteEvent;
import com.geeknewbee.doraemon.entity.event.TTSCompleteEvent;
import com.geeknewbee.doraemon.output.AddFaceType;
import com.geeknewbee.doraemon.output.BluetoothTalkTask;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.DoraemonInfoManager;
import com.geeknewbee.doraemon.processcenter.command.AddFaceCommand;
import com.geeknewbee.doraemon.processcenter.command.BluetoothCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothServiceManager {
    //远程控制命令
    public static final byte TYPE_CONTROL = 0x31;
    //开始添加人的功能
    public static final byte TYPE_PERSON_START = 0x32;
    //开始添加给人添加人脸(通过camera 获取数据)
    public static final byte TYPE_PERSON_ADD_FACE = 0x33;
    //给人设置名字
    public static final byte TYPE_PERSON_SET_NAME = 0x34;
    //开始添加给人添加人脸(通过 image 获取数据)
    public static final byte TYPE_PERSON_ADD_FACE_IMAGE = 0x35;
    //删除已经添加的所有的人
    public static final byte TYPE_PERSON_DELETE_ALL = 0x36;

    private static volatile BluetoothServiceManager instance;
    private BluetoothAdapter mBluetoothAdapter;
    private Doraemon doraemon;
    private Context context;
    private BluetoothChatService mChatService;
    private SocketService socketService;
    private BlockingQueue<byte[]> audioData = new LinkedBlockingQueue<byte[]>();
    private BluetoothTalkTask talkTask;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
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
                    if (!talkTask.hasStarted())
                        talkTask.start();

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
                case Constants.MESSAGE_SOCKET_CONTROL:
                    byte[] bytes = (byte[]) msg.obj;
                    byte funCode = 0;
                    try {
                        funCode = bytes[0];
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    switch (funCode) {
                        case BluetoothServiceManager.TYPE_CONTROL:
                            Gson gsonSecond = new Gson();
                            try {
                                BluetoothCommand command = gsonSecond.fromJson(new String(bytes, 1, bytes.length - 1), BluetoothCommand.class);
                                doraemon.addCommand(command.getCommand());
                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
                            }
                            break;
                        case BluetoothServiceManager.TYPE_PERSON_START:
                            doraemon.addCommand(new Command(CommandType.PERSON_START, new String(bytes, 1, bytes.length - 1)));
                            break;
                        case BluetoothServiceManager.TYPE_PERSON_ADD_FACE:
                            doraemon.addCommand(new AddFaceCommand(AddFaceType.YUV, Arrays.copyOfRange(bytes, 1, bytes.length)));
                            break;
                        case BluetoothServiceManager.TYPE_PERSON_ADD_FACE_IMAGE:
                            doraemon.addCommand(new AddFaceCommand(AddFaceType.IMAGE, Arrays.copyOfRange(bytes, 1, bytes.length)));
                            break;
                        case BluetoothServiceManager.TYPE_PERSON_SET_NAME:
                            doraemon.addCommand(new Command(CommandType.PERSON_SET_NAME, new String(bytes, 1, bytes.length - 1)));
                            break;
                        case BluetoothServiceManager.TYPE_PERSON_DELETE_ALL:
                            doraemon.addCommand(new Command(CommandType.PERSON_DELETE_ALL, new String(bytes, 1, bytes.length - 1)));
                            break;
                    }
                    break;
                case Constants.MESSAGE_BLE_CONTROL:
                    String message = (String) msg.obj;
                    Gson gsonSecond = new Gson();
                    try {
                        BluetoothCommand command = gsonSecond.fromJson(message, BluetoothCommand.class);
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
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        startServer();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        if (mChatService != null) {
                            mChatService.stop();
                        }
                        stopAdvertise();
                        break;
                }
            }
        }
    };

    private BluetoothServiceManager(Context context) {
        this.context = context;
        socketService = new SocketService(mHandler);
        EventBus.getDefault().register(this);
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
            mBluetoothAdapter.setName(BuildConfig.BLUETOOTH_NAME);
    }

    public void start() {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        } else {
            startServer();
        }
    }

    private void startSocketService() {
        socketService.start();
    }

    private void startServer() {
        LogUtils.d(ImmediateAlertService.TAG, "startServer");
//        DeviceUtil.setDiscoverableTimeout(1000 * 60 * 60 * 24 * 7);
//        if (mChatService == null) {
//            startBluetoothServer();
//        }

        if (BuildConfig.NEED_START_BLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
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
        stopSocketService();
    }

    private void stopSocketService() {
        socketService.stop();
    }

    /**
     * 开启经典蓝牙 sever
     */
    private void startBluetoothServer() {
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
            ias = new ImmediateAlertService(mHandler);
            mGattServer = BleUtil.getManager(context).openGattServer(context, ias);
            ias.setupServices(mGattServer);

            mBTAdvertiser.startAdvertising(
                    BleUtil.createAdvSettings(true, 0),
                    BleUtil.createFMPAdvertiseData(),
                    mAdvCallback);
        }
    }

    private void stopAdvertise() {
        LogUtils.d(ImmediateAlertService.TAG, "stopAdvertise");
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

    /**
     * 当设置wifi 完成的回调
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSetWifiComplete(SetWifiCompleteEvent event) {
        BTPostBackCommand phoneCommand = new BTPostBackCommand();
        BTPostBackCommand.SetWIFICallBack wifiCallBack = new BTPostBackCommand.SetWIFICallBack();
        wifiCallBack.isSuccess = event.isSuccess;
        wifiCallBack.content = event.content;
        wifiCallBack.hadBound = event.hadBound;
        wifiCallBack.ipAddress = event.ipAddress;
        phoneCommand.setWifiCallBack(wifiCallBack);

        //当设置wifi成功后开启socket service
        if (event.isSuccess)
            startSocketService();

        if (mChatService != null) {
            mChatService.write(new Gson().toJson(phoneCommand).getBytes());
        }

        if (ias != null) {
            ias.sendWifiNotification(new Gson().toJson(phoneCommand));
        }
        LogUtils.d("WifiSetComplete", mChatService == null ? "mChatService:Null" : "mChatService:Not Null");

        if (event.isSuccess)
            DoraemonInfoManager.getInstance(App.mContext).uploadSsid(event.SSID);
    }

    /**
     * 当设置tts 完成的回调
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onTTSComplete(TTSCompleteEvent event) {
        if (event.inputSource == SoundCommand.InputSource.IOS_BUSINESS) {
            if (ias != null) {
                ias.sendTTSNotification("END");
            }
        }
    }

    public void writeToSocket(String data) {
        if (socketService != null) {
            socketService.write(data);
        }
    }
}
