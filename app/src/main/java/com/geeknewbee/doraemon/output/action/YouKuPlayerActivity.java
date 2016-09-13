package com.geeknewbee.doraemon.output.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.entity.event.VideoCompleteEvent;
import com.geeknewbee.doraemon.entity.event.VideoPlayCreate;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.youku.player.ApiManager;
import com.youku.player.VideoQuality;
import com.youku.player.base.YoukuBasePlayerManager;
import com.youku.player.base.YoukuPlayer;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.plugin.YoukuPlayerListener;

import org.greenrobot.eventbus.EventBus;

/**
 * 优酷播放器播放界面
 */
public class YouKuPlayerActivity extends Activity implements IVideoPlayer {
    public static final String TAG = "YouKuPlayerActivity";
    public static final String EXTRA_VID = "vid";
    private YoukuBasePlayerManager basePlayerManager;
    // 播放器控件
    private YoukuPlayerView mYoukuPlayerView;

    // 需要播放的视频id
    private String vid;

    private String id = "";

    // YoukuPlayer实例，进行视频播放控制
    private YoukuPlayer youkuPlayer;

    //是否正在播放
    private boolean isPlaying;
    //是否已经调整过清晰度
    private boolean hadchangedQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.you_ku_player);
        initYouKu();
        EventBus.getDefault().post(new VideoPlayCreate(this));
        isPlaying = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 通过Intent获取播放需要的相关参数
        getVIDAndPlay(intent);
        EventBus.getDefault().post(new VideoPlayCreate(this));
        isPlaying = true;
    }

    private void getVIDAndPlay(Intent intent) {
        // 通过上个页面传递过来的Intent获取播放参数
        getIntentData(intent);

        if (TextUtils.isEmpty(id)) {
            vid = "XODQwMTY4NDg0"; // 默认视频
        } else {
            vid = id;
        }
        // 进行播放
        goPlay();
    }

    private void initYouKu() {
        // 播放器控件
        mYoukuPlayerView = (YoukuPlayerView) this
                .findViewById(R.id.youkuPlayerView);

        basePlayerManager = new YoukuBasePlayerManager(this) {
            @Override
            public void setPadHorizontalLayout() {
            }

            @Override
            public void onInitializationSuccess(YoukuPlayer player) {
                // 初始化成功后需要添加该行代码
                addPlugins();
                // 实例化YoukuPlayer实例
                youkuPlayer = player;

                getVIDAndPlay(getIntent());
            }

            @Override
            public void onSmallscreenListener() {
            }

            @Override
            public void onFullscreenListener() {
            }
        };
        basePlayerManager.onCreate();

        // 控制竖屏和全屏时候的布局参数。这两句必填。
        mYoukuPlayerView
                .setSmallScreenLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        mYoukuPlayerView
                .setFullScreenLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        // 初始化播放器相关数据
        mYoukuPlayerView.initialize(basePlayerManager);
        // 添加播放器的回调
        basePlayerManager.setPlayerListener(new YoukuPlayerListener() {
            @Override
            public void onCompletion() {
                LogUtils.d(TAG, "onCompletion");
                super.onCompletion();
                endPlay();
            }

            @Override
            public void onError(int what, int extra) {
                super.onError(what, extra);
                LogUtils.d(TAG, "onError what:" + what + " extra:" + extra);
                endPlay();
            }

            @Override
            public void onRealVideoStart() {
                LogUtils.d(TAG, "onRealVideoStart");

                super.onRealVideoStart();
                if (!hadchangedQuality) {
                    change(VideoQuality.HIGHT);
                }
            }

            @Override
            public void onTimeOut() {
                LogUtils.d(TAG, "onTimeOut");

                super.onTimeOut();
                endPlay();
            }
        });
    }

    private void endPlay() {
        isPlaying = false;
        EventBus.getDefault().post(new VideoCompleteEvent());
        finish();
    }

    private void change(VideoQuality quality) {
        try {
            hadchangedQuality = true;
            // 通过ApiManager实例更改清晰度设置，返回值（1):成功；（0): 不支持此清晰度
            // 接口详细信息可以参数使用文档
            int result = ApiManager.getInstance().changeVideoQuality(quality,
                    basePlayerManager);
            if (result == 0)
                LogUtils.d(TAG, "不支持此清晰度");
        } catch (Exception e) {
            LogUtils.d(TAG, "change:" + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LogUtils.d(TAG, "onBackPressed");
        basePlayerManager.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        basePlayerManager.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        basePlayerManager.onDestroy();
        youkuPlayer = null;
        mYoukuPlayerView.destroyDrawingCache();
        mYoukuPlayerView = null;
        EventBus.getDefault().post(new VideoPlayCreate(null));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean managerKeyDown = basePlayerManager.onKeyDown(keyCode, event);
        if (basePlayerManager.shouldCallSuperKeyDown()) {
            return super.onKeyDown(keyCode, event);
        } else {
            return managerKeyDown;
        }
    }

    @Override
    public void onLowMemory() { // android系统调用
        super.onLowMemory();
        basePlayerManager.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        basePlayerManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        basePlayerManager.onResume();
    }

    @Override
    public boolean onSearchRequested() { // android系统调用
        return basePlayerManager.onSearchRequested();
    }

    @Override
    protected void onStart() {
        super.onStart();
        basePlayerManager.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        basePlayerManager.onStop();
    }

    /**
     * 获取上个页面传递过来的数据
     */
    private void getIntentData(Intent intent) {
        if (intent != null) {
            id = intent.getStringExtra(EXTRA_VID);
        }
    }

    private void goPlay() {
        // youkuPlayer.playLocalVideo("abc",
        // "http://7xploe.media1.z0.glb.clouddn.com/XMTQ4NzA1Njc0OA==/hd1/p1_308.mp4",
        // "ceshi");
        LogUtils.d(TAG, "Play:" + vid);
        youkuPlayer.playVideo(vid);
        // XNzQ3NjcyNDc
        // XNzQ3ODU5OTgw
        // XNzUyMzkxMjE2
        // XNzU5MjMxMjcy 加密视频
        // XNzYxNzQ1MDAw 万万没想到
        // XNzgyODExNDY4 魔女范冰冰扑倒黄晓明
        // XNDcwNjUxNzcy 姐姐立正向前走
        // XNDY4MzM2MDE2 向着炮火前进
        // XODA2OTkwMDU2 卧底韦恩突出现 劫持案愈发棘手
        // XODUwODM2NTI0 会员视频
        // XODQwMTY4NDg0 一个人的武林
    }

    @Override
    public boolean init() {
        //进入Activity 自动完成init
        return true;
    }

    @Override
    public void play(Context context, String url) {
        //进入Activity就自动播放了
    }

    @Override
    public void stop() {
        if (basePlayerManager != null)
            basePlayerManager.onStop();
        finish();
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }
}
