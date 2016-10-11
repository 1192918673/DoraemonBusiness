package com.geeknewbee.doraemon.input;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.AuthRobotResponse;
import com.geeknewbee.doraemon.entity.event.ASRResultEvent;
import com.geeknewbee.doraemon.entity.event.SwitchControlTypeEvent;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.processcenter.ControlType;
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
import com.hyphenate.chat.EMOptions;
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
    public static String TAG = HYMessageReceive.class.getSimpleName();
    private static HYMessageReceive instance;
    private Context mContext;
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
            LogUtils.d(TAG, "收到消息");
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            LogUtils.d(TAG, "收到Cmd消息" + messages.toString());
            // 收到透传消息

            if (messages != null && messages.size() > 0) {
                String str = "收到Cmd消息\n";
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
            LogUtils.d(TAG, "消息已读回执");
        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> message) {
            // 收到已送达回执
            LogUtils.d(TAG, "收到已送达回执");
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            // 消息状态变动
            LogUtils.d(TAG, "消息状态变动");
        }
    };
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_SUCCESS:// 登录成功
                    //以下两个方法是为了保证进入主页面后本地会话和群组都load完毕
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    // ★★★ 登录成功后，开始监听接受消息
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    EMClient.getInstance().chatManager().addMessageListener(msgListener);
                    break;
                case LOGIN_FAILED:// 登录失败
                    hxUsername = PrefUtils.getString(mContext, Constants.KEY_HX_USERNAME, null);
                    hxPassword = PrefUtils.getString(mContext, Constants.KEY_HX_USERPWD, null);
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
                        if (NetUtils.hasNetwork(mContext)) {
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
            LogUtils.d(TAG, "收到来自" + from + "的视频呼叫");

            // 1.跳转到通话页面
            EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.CLOSE_ALL));
            Intent intent1 = new Intent(mContext, VideoTalkActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("from", from);
            mContext.startActivity(intent1);
        }
    };

    private HYMessageReceive(Context context) {
        this.mContext = context;
        EventBus.getDefault().register(this);
        EMInit();
        initLogin();
    }

    public static HYMessageReceive getInstance(Context context) {
        if (instance == null) {
            synchronized (HYMessageReceive.class) {
                if (instance == null) {
                    instance = new HYMessageReceive(context);
                }
            }
        }
        return instance;
    }

    private void initLogin() {
        // 1.获取登录需要的token、userName、pwd
        authToken = PrefUtils.getString(mContext, Constants.KEY_TOKEN, null);
        hxUsername = PrefUtils.getString(mContext, Constants.KEY_HX_USERNAME, null);
        hxPassword = PrefUtils.getString(mContext, Constants.KEY_HX_USERPWD, null);

        // 2.注册一个监听连接状态的listener
        if (null == EMClient.getInstance()) {
            EMOptions options = new EMOptions();
            // 默认添加好友时，是不需要验证的，改成需要验证
            options.setAcceptInvitationAlways(true);
            options.setAutoLogin(true);
            //初始化
            EMClient.getInstance().init(mContext, options);
            //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
            EMClient.getInstance().setDebugMode(false);
        }
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
        LogUtils.d(TAG, "authToken:" + authToken + ",hxUsername:" + hxUsername + ",hxPassword:" + hxPassword);

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
                    commands.add(new SoundCommand(readData, SoundCommand.InputSource.TIPS));
                    messageListener.onReceivedMessage(commands);
                } else if (type == 2) {// 改变声音大小命令
                    int vol = pushData.getInt("data");// 音量大小百分比，取值范围为 0-100
                    LogUtils.d(TAG, "声音大小：" + vol);
                    messageListener.onReceivedMessage(Arrays.asList(new Command(CommandType.SETTING_VOLUME, vol + "")));
                } else if (type == 3) {// 播放电影{"type":3,"data":"XMTYwODE0MjIxMg=="}
                    String readData = pushData.getString("data");
                    List<Command> commands = new ArrayList<>();
                    if (readData.equals("stop")) {
                        commands.add(new Command(CommandType.STOP));
                        messageListener.onReceivedMessage(commands);
                        return;
                    }

                    commands.add(new SoundCommand(Constants.TIP_BEFORE_PLAY_MOVIE, SoundCommand.InputSource.TIPS));
                    commands.add(new Command(CommandType.PLAY_MOVIE, readData));
                    messageListener.onReceivedMessage(commands);
                } else if (type == 4) {// 透传动作控制
                } else if (type == 5) {// 手机识别
                    if ("_open".equals(pushData.getString("data"))) {
                        LogUtils.d(TAG, "透传：_open");
                        //推出远程控制模式可以唤醒
                        EventBus.getDefault().post(new SwitchControlTypeEvent(ControlType.LOCAL));
                    } else if ("_close".equals(pushData.getString("data"))) {
                        LogUtils.d(TAG, "透传：_close");
                        //进入远程控制模式不能唤醒和监听对话
                        EventBus.getDefault().post(new SwitchControlTypeEvent(ControlType.REMOTE));
                    } else {
                        String input = "";
                        String output = "";
                        String originJson = "";
                        String inputAndOutput = pushData.getString("data");
                        if (inputAndOutput != null && !"".equals(inputAndOutput)) {
                            String inputAndOutputs[] = inputAndOutput.split("｜");
                            input = inputAndOutputs[0]; // 1.input
                            if (inputAndOutputs.length > 1) {
                                output = inputAndOutputs[1]; // 2.asrOutput
                            }
                            originJson = pushData.optString("json"); // 3.json
                        }
                        LogUtils.d(TAG, "input:" + input + ",asrOutput:" + output);
                        LogUtils.d(TAG, "json:" + originJson);

                        String asrAction = "";
                        String star_name = "";
                        String music_name = "";
                        JSONObject result = null;
                        if (!TextUtils.isEmpty(originJson)) {
                            result = new JSONObject(originJson).optJSONObject("result"); // 4.result
                            if (result != null) {
                                JSONObject info = result.optJSONObject("info"); // 5.info
                                if (info != null) {
                                    String resultType = info.optString("type"); // 6.type
                                    if ("music".equalsIgnoreCase(resultType)) {
                                        JSONObject semantics = result.optJSONObject("semantics"); // 7.semantics
                                        if (semantics != null) {
                                            JSONObject request = semantics.optJSONObject("request"); // 7.request
                                            if (request != null) {
                                                asrAction = request.optString("action"); // 8.action
                                                if ("播放音乐".equalsIgnoreCase(asrAction)) {
                                                    JSONObject param = request.optJSONObject("param"); // 9.param
                                                    star_name = param.optString("歌手");
                                                    music_name = param.optString("歌曲");
                                                } else
                                                    LogUtils.d(TAG, "手机识别：request字段中的action不等于‘播放音乐’或没有action字段！");
                                            } else
                                                LogUtils.d(TAG, "手机识别：semantics中没有request字段！");
                                        } else
                                            LogUtils.d(TAG, "手机识别：result中没有semantics字段！");
                                    } else
                                        LogUtils.d(TAG, "手机识别：info中type字段不是music或者没有type字段！");
                                } else
                                    LogUtils.d(TAG, "手机识别：result中没有info字段！");
                            } else
                                LogUtils.d(TAG, "手机识别：没有result字段！");
                        } else
                            LogUtils.d(TAG, "手机识别：整个json串为空！");

                        LogUtils.d(TAG, "input:" + input + ",output:" + output + ",action:" + asrAction + ",starName:" + star_name + ",musicName:" + music_name);
                        EventBus.getDefault().post(new ASRResultEvent(true, true, input, output, asrAction, star_name, music_name));
                    }
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
        String broadcastAction = EMClient.getInstance().callManager().getIncomingCallBroadcastAction();
        IntentFilter callFilter = new IntentFilter(broadcastAction);
        callFilter.setPriority(1000);
        mContext.registerReceiver(emIncomingCallReceiver, callFilter);
        LogUtils.d(TAG, "环信广播接受者注册。。。：" + broadcastAction);
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

    @Override
    public void destroy() {
        logout();
        mContext.unregisterReceiver(emIncomingCallReceiver);
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
