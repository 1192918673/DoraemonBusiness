package com.geeknewbee.doraemon.output.action;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import com.baseproject.utils.Logger;
import com.geeknewbee.doraemon.R;
import com.youku.player.base.YoukuBasePlayerManager;
import com.youku.player.base.YoukuPlayer;
import com.youku.player.base.YoukuPlayerView;
import com.youku.player.plugin.YoukuPlayerListener;

/**
 * 播放器播放界面，
 */
public class YuKuPlayerActivity extends Activity {
    private YoukuBasePlayerManager basePlayerManager;
    // 播放器控件
    private YoukuPlayerView mYoukuPlayerView;

    // 需要播放的视频id
    private String vid;

    private String id = "";

    // YoukuPlayer实例，进行视频播放控制
    private YoukuPlayer youkuPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.you_ku_player);
        initYouKu();
        getVIDAndPlay(getIntent(), id);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 通过Intent获取播放需要的相关参数
        getVIDAndPlay(intent, id);
    }

    private void getVIDAndPlay(Intent intent, String id) {
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
            }

            @Override
            public void onSmallscreenListener() {
            }

            @Override
            public void onFullscreenListener() {
            }
        };
        basePlayerManager.onCreate();

        // 播放器控件
        mYoukuPlayerView = (YoukuPlayerView) this
                .findViewById(R.id.youkuPlayerView);
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
                super.onCompletion();
            }
        });
    }

    @Override
    public void onBackPressed() { // android系统调用
        Logger.d("sgh", "onBackPressed before super");
        super.onBackPressed();
        Logger.d("sgh", "onBackPressed");
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
        basePlayerManager.onDestroy();
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
            id = intent.getStringExtra("vid");
        }
    }

    private void goPlay() {
        // youkuPlayer.playLocalVideo("abc",
        // "http://7xploe.media1.z0.glb.clouddn.com/XMTQ4NzA1Njc0OA==/hd1/p1_308.mp4",
        // "ceshi");

        youkuPlayer.playVideo(vid);

        // XNzQ3NjcyNDc2
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
}
