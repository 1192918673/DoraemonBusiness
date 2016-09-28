package com.geeknewbee.doraemon.view;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.input.HYMessageReceive;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMLocalSurfaceView;
import com.hyphenate.media.EMOppositeSurfaceView;

/**
 * 环信视频通话界面
 */
public class VideoTalkActivity extends Activity {

    public static final String TAG = HYMessageReceive.TAG;
    private EMCallStateChangeListener callStateChangeListener = new EMCallStateChangeListener() {
        @Override
        public void onCallStateChanged(EMCallStateChangeListener.CallState
                                               callState, EMCallStateChangeListener.CallError error) {
            switch (callState) {
                case CONNECTING: // 正在连接对方
                    LogUtils.d(TAG, "正在连接对方！");
                    break;
                case CONNECTED: // 双方已经建立连接
                    LogUtils.d(TAG, "双方已经建立连接！");
                    break;
                case ACCEPTED: // 电话接通成功
                    LogUtils.d(TAG, "电话接通成功！");
                    break;
                case DISCONNNECTED: // 电话断了
                    LogUtils.d(TAG, "电话断了！");
                    EMClient.getInstance().callManager().removeCallStateChangeListener(callStateChangeListener);
                    /**
                     * 挂断通话
                     */
                    try {
                        EMClient.getInstance().callManager().endCall();
                    } catch (EMNoActiveCallException e) {
                        e.printStackTrace();
                    }
                    finish();
                    break;
                case NETWORK_UNSTABLE: //网络不稳定
                    LogUtils.d(TAG, "网络不稳定！");
                    if (error == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                        //无通话数据
                        EMClient.getInstance().callManager().removeCallStateChangeListener(callStateChangeListener);

                        LogUtils.d(TAG, "挂断通话！");
                        /**
                         * 挂断通话
                         */
                        try {
                            EMClient.getInstance().callManager().endCall();
                        } catch (EMNoActiveCallException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                    break;
                case NETWORK_NORMAL: //网络恢复正常
                    LogUtils.d(TAG, "网络恢复正常！");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video);

        // 1.获取 CallHelper，视频通话相关的API都封装在 EMVideoCallHelper里
        EMCallManager.EMVideoCallHelper callHelper = EMClient.getInstance().callManager().getVideoCallHelper();
        callHelper.setVideoBitrate(300);// 设置视频比特率，默认150
        callHelper.setVideoOrientation(EMCallManager.EMVideoCallHelper.EMVideoOrientation.EMLandscape);// 设置通话时的屏幕方向（注意在 mainfest 也需设置相应 activity 的方向，需和此保持一致）

        // 2.注册通话状态监听
        EMClient.getInstance().callManager().addCallStateChangeListener(callStateChangeListener);

        // 3.两个 SurfaceView
        EMLocalSurfaceView localSurface = (EMLocalSurfaceView) findViewById(R.id.localSurfaceView);
        EMOppositeSurfaceView oppositeSurface = (EMOppositeSurfaceView) findViewById(R.id.oppositeSurfaceView);
        EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);

        // 4.设置摄像头方向 后置摄像头
        try {
            EMClient.getInstance().callManager().setCameraFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        EMClient.getInstance().callManager().setCameraDataProcessor(new MyDataProcessor());

        /**
         * 5.接听通话
         * @throws EMNoActiveCallException
         * @throws EMNetworkUnconnectedException
         */
        try {
            EMClient.getInstance().callManager().answerCall();
        } catch (EMNoActiveCallException e) {
            e.printStackTrace();
        }
    }

    /**
     * 视频通话处理本地摄像头数数据
     */
    private class MyDataProcessor implements EMCallManager.EMCameraDataProcessor {
        @Override
        public void onProcessData(byte[] data, Camera camera, int width, int height) {
            //把data中的数据替换成自己处理过的
            camera.setDisplayOrientation(180);
        }
    }
}
