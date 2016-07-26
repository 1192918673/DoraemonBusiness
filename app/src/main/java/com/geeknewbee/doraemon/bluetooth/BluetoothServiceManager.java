package com.geeknewbee.doraemon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.geeknewbee.doraemon.control.Doraemon;
import com.geeknewbee.doraemon.util.Constant;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


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
    private BlockingQueue<byte[]> TestDatas = new LinkedBlockingQueue<byte[]>();
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
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
                case Constant.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constant.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
//                    String readMessage = new String(readBuf, 0, msg.arg1);
//                    Gson gson = new Gson();
//                    try {
//                        BluetoothCommand command = gson.fromJson(readMessage, BluetoothCommand.class);
//                        doraemon.addCommand(command.getCommand());
//                    } catch (JsonSyntaxException e) {
//                        e.printStackTrace();
//                    }
                    synchronized (this) {
//                        short[] aShort = BytesUtils.getShort(readBuf);
                        TestDatas.add(readBuf);
                    }
                    break;
                case Constant.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    break;
                case Constant.MESSAGE_TOAST:
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
        new PlayTask().execute();
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
    }

    private void setupChat() {
        if (mChatService == null)
            mChatService = new BluetoothChatService(context, mHandler);
        if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
            // Start the Bluetooth chat services
            mChatService.start();
        }
    }

    class PlayTask extends AsyncTask<Void, Integer, Void> {

        private AudioTrack track;

        @Override
        protected Void doInBackground(Void... arg0) {
            int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            try {
                //定义输入流，将音频写入到AudioTrack类中，实现播放
//				DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFile)));
                //实例AudioTrack
                track = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2, AudioTrack.MODE_STREAM);
                //开始播放
                track.play();
                //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                byte[] poll;
                while (true) {
                    poll = TestDatas.poll(100, TimeUnit.MILLISECONDS);
                    Log.d("PlayTask", "play data:" + (poll == null ? "null" : "have data"));
                    if (poll != null)
                        //然后将数据写入到AudioTrack中
                        track.write(poll, 0, poll.length);
                }
            } catch (Exception e) {
                Log.d("PlayTask", "Exception=" + e.getMessage());
            } finally {
                //播放结束
                track.stop();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            track.release();
        }

        protected void onPreExecute() {
        }
    }
}
