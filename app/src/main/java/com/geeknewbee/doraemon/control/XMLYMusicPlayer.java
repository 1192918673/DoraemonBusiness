package com.geeknewbee.doraemon.control;

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

    private XmPlayerManager mPlayerManager;
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
            PlayableModel model = mPlayerManager.getCurrSound();
            if (model != null) {
                String title = null;
                String msg = null;
                String coverUrl = null;
                String coverSmall = null;
                if (model instanceof Track) {
                    Track info = (Track) model;
                    title = info.getTrackTitle();
                    msg = info.getAnnouncer() == null ? "" : info.getAnnouncer().getNickname();
                    coverUrl = info.getCoverUrlLarge();
                    coverSmall = info.getCoverUrlMiddle();
                } else if (model instanceof Schedule) {
                    Schedule program = (Schedule) model;
                    msg = program.getRelatedProgram().getProgramName();
                    title = program.getRelatedProgram().getProgramName();
                    coverUrl = program.getRelatedProgram().getBackPicUrl();
                    coverSmall = program.getRelatedProgram().getBackPicUrl();
                } else if (model instanceof Radio) {
                    Radio radio = (Radio) model;
                    title = radio.getRadioName();
                    msg = radio.getRadioDesc();
                    coverUrl = radio.getCoverUrlLarge();
                    coverSmall = radio.getCoverUrlSmall();
                }
            }
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

        CommonRequest.getSearchedTracks(map, new IDataCallBack<SearchTrackList>() {

            @Override
            public void onSuccess(SearchTrackList searchTrackList) {
                LogUtils.d("搜索声音数量", searchTrackList.getTracks().size() + "");
                LogUtils.d("搜索声音", searchTrackList.getTracks().get(0).toString());
                mPlayerManager.playList(searchTrackList.getTracks(), 0);
            }

            @Override
            public void onError(int code, String message) {
                LogUtils.d("搜索声音", message);
            }
        });
        return true;
    }

    @Override
    public boolean stop() {
        if (mPlayerManager != null) {
            mPlayerManager.stop();
            mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
            mPlayerManager.release();
        }
        return true;
    }
}
