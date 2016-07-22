package com.geeknewbee.doraemon.control;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.control.base.IMusicPlayer;
import com.geeknewbee.doraemon.util.Constant;
import com.geeknewbee.doraemon.util.LogUtils;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.schedule.Schedule;
import com.ximalaya.ting.android.opensdk.model.track.SearchTrackList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ACER on 2016/7/22.
 */
public class XMLYMusicPlayer implements IMusicPlayer {

    private String mAppSecret = "03bf352a5a7d03ed3f8348d8c8281630";
    private XmPlayerManager mPlayerManager;
    private CommonRequest mXimalaya;
    private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener() {

        @Override
        public void onSoundPrepared() {
            LogUtils.d(Constant.TAG_MUSIC, "onSoundPrepared");
        }

        @Override
        public void onPlayStart() {
            LogUtils.d(Constant.TAG_MUSIC, "onPlayStart");
        }

        @Override
        public void onPlayProgress(int currPos, int duration) {
            String title = "";
            PlayableModel info = mPlayerManager.getCurrSound();
            if (info != null) {
                if (info instanceof Track) {
                    title = ((Track) info).getTrackTitle();
                } else if (info instanceof Schedule) {
                    title = ((Schedule) info).getRelatedProgram().getProgramName();
                } else if (info instanceof Radio) {
                    title = ((Radio) info).getRadioName();
                }
            }
        }

        @Override
        public void onPlayPause() {
            LogUtils.d(Constant.TAG_MUSIC, "onPlayPause");
        }

        @Override
        public void onSoundPlayComplete() {
            LogUtils.d(Constant.TAG_MUSIC, "onSoundPlayComplete");
        }

        @Override
        public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel) {
            LogUtils.d(Constant.TAG_MUSIC, "onSoundSwitch index:");
        }

        @Override
        public void onPlayStop() {
            LogUtils.d(Constant.TAG_MUSIC, "onPlayStop");
        }

        @Override
        public boolean onError(XmPlayerException exception) {
            LogUtils.d(Constant.TAG_MUSIC, "onError " + exception.getMessage());
            return false;
        }

        @Override
        public void onBufferingStart() {
            LogUtils.d(Constant.TAG_MUSIC, "onBufferingStart");
        }

        @Override
        public void onBufferProgress(int position) {

        }

        @Override
        public void onBufferingStop() {
            LogUtils.d(Constant.TAG_MUSIC, "onBufferingStop");
        }
    };
    private IXmAdsStatusListener mAdsListener = new IXmAdsStatusListener() {

        @Override
        public void onStartPlayAds(Advertis ad, int position) {
            LogUtils.d(Constant.TAG_MUSIC, "onStartPlayAds, Ad:" + ad.getName() + ", pos:" + position);
        }

        @Override
        public void onStartGetAdsInfo() {
            LogUtils.d(Constant.TAG_MUSIC, "onStartGetAdsInfo");
        }

        @Override
        public void onGetAdsInfo(AdvertisList ads) {
            LogUtils.d(Constant.TAG_MUSIC, "onGetAdsInfo " + (ads != null));
        }

        @Override
        public void onError(int what, int extra) {
            LogUtils.d(Constant.TAG_MUSIC, "onError what:" + what + ", extra:" + extra);
        }

        @Override
        public void onCompletePlayAds() {
            LogUtils.d(Constant.TAG_MUSIC, "onCompletePlayAds");
            PlayableModel model = mPlayerManager.getCurrSound();
        }

        @Override
        public void onAdsStopBuffering() {
            LogUtils.d(Constant.TAG_MUSIC, "onAdsStopBuffering");
        }

        @Override
        public void onAdsStartBuffering() {
            LogUtils.d(Constant.TAG_MUSIC, "onAdsStartBuffering");
        }
    };

    public XMLYMusicPlayer() {
        init();
    }

    private void init() {
        mXimalaya = CommonRequest.getInstanse();
        mXimalaya.init(App.mContext, mAppSecret);
        mXimalaya.setDefaultPagesize(50);

        mPlayerManager = XmPlayerManager.getInstance(App.mContext);
        mPlayerManager.init();
        mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
        mPlayerManager.addAdsStatusListener(mAdsListener);
        mPlayerManager.getPlayerStatus();
    }

    @Override
    public boolean play(String param) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.SEARCH_KEY, param);
        map.put(DTransferConstants.CATEGORY_ID, "2");
        map.put(DTransferConstants.PAGE, "1");
        map.put(DTransferConstants.CALC_DIMENSION, "1");

        ConnectivityManager mgrConn = (ConnectivityManager) App.mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager) App.mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        boolean isConnected = ((mgrConn.getActiveNetworkInfo() != null && mgrConn
                .getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || mgrTel
                .getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
        LogUtils.d("网络状态", isConnected + "");

        CommonRequest.getSearchedTracks(map, new IDataCallBack<SearchTrackList>() {

            @Override
            public void onSuccess(SearchTrackList searchTrackList) {
                LogUtils.d("搜索声音数量", searchTrackList.getTracks().size() + "");
                LogUtils.d("搜索声音", searchTrackList.getTracks().get(0).toString());
                mPlayerManager.playList(searchTrackList.getTracks(), 0);
            }

            @Override
            public void onError(int code, String message) {
                LogUtils.d("搜索声音", "code:" + code);
            }
        });
        return true;
    }

    @Override
    public boolean stop() {
        if (mPlayerManager != null) {
            mPlayerManager.stop();
            mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
        }
        return true;
    }

    @Override
    public void release() {
        if (mPlayerManager != null) {
            mPlayerManager.release();
        }
    }
}
