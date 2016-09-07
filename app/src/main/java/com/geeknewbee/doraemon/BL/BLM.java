package com.geeknewbee.doraemon.BL;

import android.content.Context;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.GetAnswerResponse;
import com.geeknewbee.doraemon.entity.event.BLLocalResponse;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.google.gson.Gson;

import cn.com.broadlink.blnetwork.BLNetwork;

/**
 * Created by GYY on 2016/9/6.
 */
public class BLM {

    private static BLNetwork blNetwork;

    private static final int MSG_WHAT_BL_INIT = 100;
    private static final int MSG_WHAT_BL_SEND = 105;
    private static final int MSG_WHAT_BL_PLUG = 112;

    private static String TAG = "BLM";

    /**
     * 初始化博联设备
     */
    public static void initBroadLink(Context context) {
        try {
            blNetwork = BLNetwork.getInstanceBLNetwork(context);
            broadLinkInit(1000);
        } catch (Exception e) {
            LogUtils.d( TAG, "博联设备初始化错误：" + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * 初始化BroadLink的网络
     */
    public static void broadLinkInit(int delay) {
        BLLocalCMD blLocalCMD = new BLLocalCMD();
        blLocalCMD.setApi_id(1);
        blLocalCMD.setCommand("network_init");
        blLocalCMD.put("license", Constants.BROADLINK_USER_LICENSE);
        blLocalCMD.put("type_license", Constants.BROADLINK_TYPE_LICENSE);
        sendCMDToBroadLink(blLocalCMD.getCMDString(), MSG_WHAT_BL_INIT, delay, 0);
    }

    /**
     * 发送命令代码到设备
     */
    public static void broadLinkRMProSend( String blMac, String cmd_data, int delay) {
        BLLocalCMD blLocalCMD = new BLLocalCMD();
        blLocalCMD.setApi_id(134);
        blLocalCMD.setCommand("rm2_send");
        blLocalCMD.put("mac", blMac);
        String _data = null;
        if (cmd_data.length() > 900) {
            _data = cmd_data.substring(0, 890);
        } else {
            _data = cmd_data;
        }
        blLocalCMD.put("data", _data);
        sendCMDToBroadLink(blLocalCMD.getCMDString(), MSG_WHAT_BL_SEND, delay, 0);
    }

    /**
     * 发送命令代码到设备
     */
    public static void broadLinkRMProSend(GetAnswerResponse response) {
        if(response.getType() == 1) {   //电视
            String[] mData = response.getData().split(",");
            if (null != mData && mData.length == 2) {
                LogUtils.d("SoundTranslateTaskQueue","操作电视：" + response.getData());
                broadLinkRMProSend(mData[0], mData[1], 100);
                broadLinkRMProSend(mData[0], mData[1], 200);
                broadLinkRMProSend(mData[0], mData[1], 200);
            }
        }else if (response.getType() == 3) {   //窗帘操作
            String[] mData= response.getData().split(",");
            if (null != mData && mData.length == 2) {
                LogUtils.d("SoundTranslateTaskQueue", "操作窗帘：" + response.getData());
                broadLinkRMProSend(mData[0], mData[1], 1000);
            }
        } else if (response.getType() == 4) {   //射频开关操作
            String[] mData = response.getData().split(",");
            if (null != mData && mData.length == 2) {
                LogUtils.d("SoundTranslateTaskQueue","操作射频开关：" + response.getData());
                broadLinkRMProSend(mData[0], mData[1], 1000);
            }
        }
    }

    /**
     * 发送命令代码到设备
     */
    public static void broadLinkRMProSend( String data, int delay) {
        String[] mData = data.split(",");
        if (null != mData && mData.length == 2) {
            broadLinkRMProSend(mData[0], mData[1], delay);
        }
    }

    /**
     * 设置插座状态
     *
     * @param status 0，关闭，1，打开
     */
    public static void modifyPlugbase(String blMac, int status) {
        BLLocalCMD blLocalCMD = new BLLocalCMD();
        blLocalCMD.setApi_id(72);
        blLocalCMD.setCommand("sp2_control");
        blLocalCMD.put("mac", blMac);
        blLocalCMD.put("status", status);
        sendCMDToBroadLink(blLocalCMD.getCMDString(), MSG_WHAT_BL_PLUG, 1000, 0);
    }

    /**
     * 操作插座
     * @param input 输入的语音
     * @param blMac 插座的mac
     */
    public static void modifyPlugbase(String input, String blMac) {
        if (input.indexOf("开") != -1) {
            LogUtils.d("SoundTranslateTaskQueue","打开插座：" + blMac);
            modifyPlugbase(blMac, 1);
        } else {
            LogUtils.d("SoundTranslateTaskQueue","关闭插座：" + blMac);
            modifyPlugbase(blMac, 0);
        }
    }



    /**
     * 发送命令给BroadLink的设备
     *
     * @param cmd       命令串
     * @param msg_what  通知的消息类别
     * @param delay     推迟执行的时间
     * @param bls1_what 消息类别
     */
    public static void sendCMDToBroadLink(final String cmd, final int msg_what, final int delay, final int bls1_what) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LogUtils.i("MainActivity","BroadLink发送命令：" + cmd);
                String blResponse = blNetwork.requestDispatch(cmd);
                LogUtils.i("MainActivity","BroadLink返回：" + blResponse);

                BLLocalResponse response = new Gson().fromJson(blResponse, BLLocalResponse.class);
                switch (msg_what) {
                    case MSG_WHAT_BL_INIT:
                        if (response.getCode() == 0 || response.getCode() == 3) {
                            LogUtils.d(TAG, "BroadLink初始化成功");
                        } else {
                            LogUtils.d(TAG,"BroadLink初始化失败：" + response.getMsg());
                            broadLinkInit(1000);
                        }
                        break;
                    case MSG_WHAT_BL_PLUG:
                    case MSG_WHAT_BL_SEND:
                        if (response.getCode() == 0) {
                            LogUtils.d(TAG, "BroadLink SEND返回成功：" + response.getMsg());
                        } else {
                            LogUtils.d(TAG, "BroadLink SEND失败：" + response.getMsg());
                        }
                        break;

                }
            }
        };
        thread.start();
    }
}
