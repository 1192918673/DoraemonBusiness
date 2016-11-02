package com.geeknewbee.doraemon.view;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.easemob.chat.EMCallStateChangeListener;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMVideoCallHelper;
import com.facebook.stetho.common.LogUtil;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.input.HYMessageReceive;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.utils.CameraHelper;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.UUID;

/**
 * 环信视频通话界面
 */
public class VideoTalkActivity extends Activity {

    public static final String TAG = HYMessageReceive.TAG;
    private SurfaceHolder localSurfaceHolder;
    private CameraHelper cameraHelper;
    private SurfaceHolder oppositeSurfaceHolder;
    private EMVideoCallHelper callHelper;
    private String username;
    private AudioManager audioManager;
    private SurfaceView oppositeSurface;
    private boolean monitor = true;
    private LinearLayout rootContainer;
    private String msgid;
    private SurfaceView localSurface;
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
                    startMonitor();// 调试过程用
                    break;
                case DISCONNNECTED: // 电话断了
                    LogUtils.d(TAG, "电话断了！");
                    final CallError fError = error;
                    EMChatManager.getInstance().removeCallStateChangeListener(callStateChangeListener);
                    /**
                     * 挂断通话
                     */
                    try {
                        EMChatManager.getInstance().endCall();
                        closeSpeakerOn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            String s1 = "The Call was refused";
                            String s2 = "Connection failed";
                            String s3 = "The peer is not online now, please try later";
                            String s4 = "The peer is busy now, please try later";
                            String s5 = "The peer did not answer";
                            String s6 = "Hang up";
                            String s7 = "The peer has ended the call";
                            String s8 = "did not answer";
                            String s9 = "Cancelled";

                            if (fError == CallError.REJECTED) {
                                LogUtils.d(TAG, s1);
                            } else if (fError == CallError.ERROR_TRANSPORT) {
                                LogUtils.d(TAG, s2);
                            } else if (fError == CallError.ERROR_INAVAILABLE) {
                                LogUtils.d(TAG, s3);
                            } else if (fError == CallError.ERROR_BUSY) {
                                LogUtils.d(TAG, s4);
                            } else if (fError == CallError.ERROR_NORESPONSE) {
                                LogUtils.d(TAG, s5);
                            } else {
                            }
                        }
                    }.start();
                    finish();
                    break;
                case NETWORK_UNSTABLE: //网络不稳定
                    LogUtils.d(TAG, "网络不稳定！");
                    if (error == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
                        //无通话数据
                        EMChatManager.getInstance().removeCallStateChangeListener(callStateChangeListener);

                        LogUtils.d(TAG, "挂断通话！");
                        /**
                         * 挂断通话
                         */
                        try {
                            EMChatManager.getInstance().endCall();
                        } catch (Exception e) {
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
        if (savedInstanceState != null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_video);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        rootContainer = (LinearLayout) findViewById(R.id.root_layout);
        username = getIntent().getStringExtra("from");
        msgid = UUID.randomUUID().toString();
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_RINGTONE);
        audioManager.setSpeakerphoneOn(true);

        // 1.显示本地图像的SurfaceView
        localSurface = (SurfaceView) findViewById(R.id.localSurfaceView);
        localSurface.setVisibility(View.INVISIBLE);
        localSurfaceHolder = localSurface.getHolder();
        /*Canvas canvas = oppositeSurfaceHolder.lockCanvas();
        canvas.rotate(90);
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        oppositeSurface.measure(w, h);
        int height =oppositeSurface.getMeasuredHeight();
        int width =oppositeSurface.getMeasuredWidth();
        canvas.translate(0, width);*/

        // 2.获取CallHelper CameraHelper，视频通话相关的API都封装在 EMVideoCallHelper里
        callHelper = EMVideoCallHelper.getInstance();
        //callHelper.setVideoBitrate(300);// 设置视频比特率，默认150
        callHelper.setVideoOrientation(EMVideoCallHelper.EMVideoOrientation.EMLandscape);// 设置通话时的屏幕方向（注意在 mainfest 也需设置相应 activity 的方向，需和此保持一致）
        cameraHelper = new CameraHelper(callHelper, localSurfaceHolder);

        // 3.显示对方图像的SurfaceView
        oppositeSurface = (SurfaceView) findViewById(R.id.oppositeSurfaceView);
        oppositeSurfaceHolder = oppositeSurface.getHolder();
        callHelper.setSurfaceView(oppositeSurface); // 设置显示对方图像的SurfaceView

        // 4.注册SurfaceView监听
        localSurfaceHolder.addCallback(new LocalCallback());
        oppositeSurfaceHolder.addCallback(new OppositeCallback());

        // 5.注册通话状态监听
        EMChatManager.getInstance().addCallStateChangeListener(callStateChangeListener);

        // 6.接听通话
        try {
            EMChatManager.getInstance().answerCall();
            openSpeakerOn();
            cameraHelper.setStartFlag(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 打开扬声器
    protected void openSpeakerOn() {
        try {
            if (!audioManager.isSpeakerphoneOn())
                audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 关闭扬声器
    protected void closeSpeakerOn() {

        try {
            if (audioManager != null) {
                // int curVolume =
                // audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
                if (audioManager.isSpeakerphoneOn())
                    audioManager.setSpeakerphoneOn(false);
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                // audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                // curVolume, AudioManager.STREAM_VOICE_CALL);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        LogUtils.d(TAG, "视频结束，开启人脸检测，切换到等待唤醒状态");
        stopMonitor();
        try {
            callHelper.setSurfaceView(null);
            oppositeSurface = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setMicrophoneMute(false);
        Doraemon.getInstance(this).startReceive();// 开启人脸检测
        Doraemon.getInstance(this).switchSoundMonitor(SoundMonitorType.EDD);
        super.onDestroy();
    }

    void startMonitor() {
        new Thread(new Runnable() {
            public void run() {
                while (monitor) {
                    LogUtils.d(TAG, "宽x高：" + callHelper.getVideoWidth() + "x" + callHelper.getVideoHeight()
                            + "\n延迟：" + callHelper.getVideoTimedelay()
                            + "\n帧率：" + callHelper.getVideoFramerate()
                            + "\n丢包数：" + callHelper.getVideoLostcnt()
                            + "\n本地比特率：" + callHelper.getLocalBitrate()
                            + "\n对方比特率：" + callHelper.getRemoteBitrate());
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    void stopMonitor() {
        monitor = false;
    }

    /**
     * 本地SurfaceHolder callback
     */
    class LocalCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            cameraHelper.startCapture();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    /**
     * 对方SurfaceHolder callback
     */
    class OppositeCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            callHelper.onWindowResize(width, height, format);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }
}
