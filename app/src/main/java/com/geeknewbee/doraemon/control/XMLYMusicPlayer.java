package com.geeknewbee.doraemon.control;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.control.base.IMusicPlayer;
import com.geeknewbee.doraemon.utils.LogUtils;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 喜马拉雅 音乐
 */
public class XMLYMusicPlayer implements IMusicPlayer {

    private String mAppSecret = "03bf352a5a7d03ed3f8348d8c8281630";
    private XmPlayerManager mPlayerManager;// 播放器
    private CommonRequest mXimalaya; // 命令请求对象
    private List<Track> tracks = new ArrayList<>();
    private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener() {

        @Override
        public void onSoundPrepared() {
            LogUtils.d(Constants.TAG_MUSIC, "onSoundPrepared");
        }

        @Override
        public void onPlayStart() {
            LogUtils.d(Constants.TAG_MUSIC, "onPlayStart");
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
            LogUtils.d(Constants.TAG_MUSIC, "onPlayPause");
        }

        @Override
        public void onSoundPlayComplete() {
            LogUtils.d(Constants.TAG_MUSIC, "onSoundPlayComplete");
        }

        @Override
        public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel) {
            LogUtils.d(Constants.TAG_MUSIC, "onSoundSwitch index:");
            mPlayerManager.clearPlayCache();
            if (laModel != null) {
                laModel.setLastPlayedMills(0);
            }
            curModel.setLastPlayedMills(0);
        }

        @Override
        public void onPlayStop() {
            LogUtils.d(Constants.TAG_MUSIC, "onPlayStop");
        }

        @Override
        public boolean onError(XmPlayerException exception) {
            LogUtils.d(Constants.TAG_MUSIC, "onError " + exception.getMessage());
            return false;
        }

        @Override
        public void onBufferingStart() {
            LogUtils.d(Constants.TAG_MUSIC, "onBufferingStart");
        }

        @Override
        public void onBufferProgress(int position) {

        }

        @Override
        public void onBufferingStop() {
            LogUtils.d(Constants.TAG_MUSIC, "onBufferingStop");
        }
    };
    private IXmAdsStatusListener mAdsListener = new IXmAdsStatusListener() {

        @Override
        public void onStartPlayAds(Advertis ad, int position) {
            LogUtils.d(Constants.TAG_MUSIC, "onStartPlayAds, Ad:" + ad.getName() + ", pos:" + position);
        }

        @Override
        public void onStartGetAdsInfo() {
            LogUtils.d(Constants.TAG_MUSIC, "onStartGetAdsInfo");
        }

        @Override
        public void onGetAdsInfo(AdvertisList ads) {
            LogUtils.d(Constants.TAG_MUSIC, "onGetAdsInfo " + (ads != null));
        }

        @Override
        public void onError(int what, int extra) {
            LogUtils.d(Constants.TAG_MUSIC, "onError what:" + what + ", extra:" + extra);
        }

        @Override
        public void onCompletePlayAds() {
            LogUtils.d(Constants.TAG_MUSIC, "onCompletePlayAds");
            PlayableModel model = mPlayerManager.getCurrSound();
        }

        @Override
        public void onAdsStopBuffering() {
            LogUtils.d(Constants.TAG_MUSIC, "onAdsStopBuffering");
        }

        @Override
        public void onAdsStartBuffering() {
            LogUtils.d(Constants.TAG_MUSIC, "onAdsStartBuffering");
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

        /*ConnectivityManager mgrConn = (ConnectivityManager) App.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager) App.mContext.getSystemService(Context.TELEPHONY_SERVICE);
        boolean isConnected = ((mgrConn.getActiveNetworkInfo() != null
                && mgrConn.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED)
                || mgrTel.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS);
        LogUtils.d("网络状态", isConnected + "");*/

        CommonRequest.getSearchedTracks(map, new IDataCallBack<SearchTrackList>() {

            @Override
            public void onSuccess(SearchTrackList searchTrackList) {
                LogUtils.d("搜索声音数量", searchTrackList.getTracks().size() + "");
                //LogUtils.d("搜索声音", searchTrackList.getTracks().get(0).toString());
                if (searchTrackList.getTracks() != null && searchTrackList.getTracks().size() > 0) {
                    tracks.clear();
                    tracks.add(searchTrackList.getTracks().get(0));
                    mPlayerManager.clearPlayCache();
                    mPlayerManager.seekTo(0);
                    mPlayerManager.playList(tracks, 0);
                }
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
