package com.geeknewbee.doraemon.input;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.AuthRobotResponse;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.utils.PrefUtils;
import com.geeknewbee.doraemon.view.VideoTalkActivity;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.NetUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 监听环信数据推送
 */
public class HYMessageReceive implements IMessageReceive {

    public static final int LOGIN_SUCCESS = 0;
    public static final int LOGIN_FAILED = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_DISCONNECTED = 3;
    private static HYMessageReceive instance;
    public String TAG = HYMessageReceive.class.getSimpleName();
    private String authToken = null;
    private String hxUsername;
    private String hxPassword;
    private boolean isLogined;
    private IMessageReceive.MessageListener messageListener;
    /**
     * 消息接受监听
     */
    private EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            // 收到消息
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            LogUtils.d(TAG, messages.toString());
            // 收到透传消息

            if (messages != null && messages.size() > 0) {
                String str = "收到消息\n";
                for (int i = 0; i < messages.size(); i++) {
                    String action = ((EMCmdMessageBody) messages.get(i).getBody()).action();
                    str += messages.get(i).getFrom() + " : " + action + "\n";
                    parseEMCMDData(action);
                }
                LogUtils.d(TAG, str);
            }
        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> messages) {
            // 收到已读回执
        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> message) {
            // 收到已送达回执
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            // 消息状态变动
        }
    };
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_SUCCESS:// 登录成功
                    // ★★★ 登录成功后，开始监听接受消息
                    EMClient.getInstance().chatManager().addMessageListener(msgListener);
                    break;
                case LOGIN_FAILED:// 登录失败
                    hxUsername = PrefUtils.getString(App.mContext, Constants.KEY_HX_USERNAME, null);
                    hxPassword = PrefUtils.getString(App.mContext, Constants.KEY_HX_USERPWD, null);
                    EMLogin(hxUsername, hxPassword);
                    break;
                case STATE_CONNECTED:// 已连接

                    break;
                case STATE_DISCONNECTED:// 断开连接
                    int error = msg.arg1;
                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除
                        LogUtils.d(TAG, "显示帐号已经被移除");
                    } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        // 显示帐号在其他设备登陆
                        LogUtils.d(TAG, "显示帐号在其他设备登陆");
                    } else {
                        if (NetUtils.hasNetwork(App.mContext)) {
                            //连接不到聊天服务器
                            LogUtils.d(TAG, "连接不到聊天服务器");
                        } else {
                            //当前网络不可用，请检查网络设置
                            LogUtils.d(TAG, "当前网络不可用，请检查网络设置");
                        }
                    }
                    break;
            }
        }
    };
    // 接受视频呼叫的广播接受者
    private BroadcastReceiver emIncomingCallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 拨打方username
            String from = intent.getStringExtra("from");
            // call type
            String type = intent.getStringExtra("type");
            // 跳转到通话页面
            Intent intent1 = new Intent(App.mContext, VideoTalkActivity.class);
            intent1.putExtra("from", from);
            App.mContext.startActivity(intent1);
            // TODO 发送切换成EDD监听 是否再停用EDD监听？视频挂断在启动监听
            EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
        }
    };

    private HYMessageReceive() {
        EventBus.getDefault().register(this);
        initLogin();
    }

    public static HYMessageReceive getInstance() {
        if (instance == null) {
            synchronized (HYMessageReceive.class) {
                if (instance == null) {
                    instance = new HYMessageReceive();
                }
            }
        }
        return instance;
    }

    private void initLogin() {
        // 1.获取登录需要的token、userName、pwd
        authToken = PrefUtils.getString(App.mContext, Constants.KEY_TOKEN, null);
        hxUsername = PrefUtils.getString(App.mContext, Constants.KEY_HX_USERNAME, null);
        hxPassword = PrefUtils.getString(App.mContext, Constants.KEY_HX_USERPWD, null);
        LogUtils.d(TAG, "authToken:" + authToken + ",hxUsername:" + hxUsername + ",hxPassword:" + hxPassword);

        // 2.注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyEMConnectionListener());

        // 2.登录
        if (!isLogined) {
            EMLogin(hxUsername, hxPassword);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateHXInfo(AuthRobotResponse.HxUserInfo userInfo) {
        if (!isLogined) {
            EMLogin(userInfo.getUsername(), userInfo.getPassword());
        }
    }

    /**
     * 环信登录
     *
     * @param name
     * @param pwd
     */
    private void EMLogin(String name, String pwd) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            mHandler.sendEmptyMessageDelayed(LOGIN_FAILED, 5000);
            return;
        }

        EMClient.getInstance().login(name, pwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                LogUtils.d(TAG, "登陆聊天服务器成功！");
                mHandler.sendEmptyMessage(LOGIN_SUCCESS);
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(int code, String message) {
                LogUtils.d(TAG, "登陆聊天服务器失败,5秒后重试！" + message);
                mHandler.sendEmptyMessageDelayed(LOGIN_FAILED, 5000);
            }
        });

    }

    /**
     * 解析收到的透传消息
     *
     * @param action
     */
    private void parseEMCMDData(String action) {
        try {
            if (action != null) {
                JSONObject pushData = new JSONObject(action);
                int type = pushData.getInt("type");
                if (type == 1) {// 文字透传
                    String readData = pushData.getString("data");
                    List<Command> commands = new ArrayList<>();
                    commands.add(new SoundCommand("定时提醒：" + readData, SoundCommand.InputSource.TIPS));
                    messageListener.onReceivedMessage(commands);
                } else if (type == 2) {// 改变声音大小命令
                    int vol = pushData.getInt("data");// 音量大小百分比，取值范围为 0-100
                    messageListener.onReceivedMessage(Arrays.asList(new Command(CommandType.SETTING_VOLUME, vol + "")));
                } else if (type == 3) {// 播放电影{"type":3,"data":"XMTYwODE0MjIxMg=="}
                } else if (type == 4) {// 透传动作控制
                } else if (type == 5) {// 手机识别
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    /**
     * 环信视频通话逻辑 初始化操作
     */
    private void EMInit() {
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        App.mContext.registerReceiver(emIncomingCallReceiver, callFilter);
    }

    /**
     * 退出登录
     */
    public void logout() {
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onProgress(int progress, String status) {
            }

            @Override
            public void onError(int code, String message) {
            }
        });
    }

    /**
     * 环信连接状态监听
     */
    private class MyEMConnectionListener implements EMConnectionListener {

        @Override
        public void onConnected() {
            isLogined = true;
        }

        @Override
        public void onDisconnected(int error) {
            isLogined = false;
            mHandler.obtainMessage(STATE_DISCONNECTED, error, -1);
        }
    }
}
