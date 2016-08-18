package com.geeknewbee.doraemon.output.action;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.AlbumList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.schedule.Schedule;
import com.ximalaya.ting.android.opensdk.model.track.SearchTrackList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerConfig;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 喜马拉雅 音乐
 */
public class XMLYMusicPlayer implements IMusicPlayer {

    private String mAppSecret = "03bf352a5a7d03ed3f8348d8c8281630";
    private XmPlayerManager mPlayerManager;// 播放器
    private CommonRequest mXimalaya; // 命令请求对象
    private List<Track> tracks = new ArrayList<>();
    private List<Long> albumId = new ArrayList<>();
    private MusicListener listener;
    //正在播放标示
    private boolean isPlaying;

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
            notifyComplete();
        }

        @Override
        public void onSoundPlayComplete() {
            LogUtils.d(Constants.TAG_MUSIC, "onSoundPlayComplete");
            notifyComplete();
        }

        @Override
        public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel) {
            LogUtils.d(Constants.TAG_MUSIC, "onSoundSwitch index:");
        }

        @Override
        public void onPlayStop() {
            LogUtils.d(Constants.TAG_MUSIC, "onPlayStop");
            notifyComplete();
        }

        @Override
        public boolean onError(XmPlayerException exception) {
            LogUtils.d(Constants.TAG_MUSIC, "onError " + exception.getMessage());
            notifyComplete();
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

    public XMLYMusicPlayer() {
        init();
    }

    private void init() {
        mXimalaya = CommonRequest.getInstanse();
        mXimalaya.init(BaseApplication.mContext, mAppSecret);
        mXimalaya.setDefaultPagesize(50);

        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.mContext);
        mPlayerManager.init();
        XmPlayerConfig.getInstance(BaseApplication.mContext).setBreakpointResume(false);
        mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
        mPlayerManager.getPlayerStatus();

        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.CATEGORY_ID, "4");
        map.put(DTransferConstants.CALC_DIMENSION, "3");
        map.put(DTransferConstants.TAG_NAME, "冷笑话");
        CommonRequest.getAlbumList(map, new IDataCallBack<AlbumList>() {
            @Override
            public void onSuccess(AlbumList albumList) {
                StringBuffer sb = new StringBuffer();
                List<Album> list = albumList.getAlbums();
                albumId.clear();
                for (Album album : list) {
                    albumId.add(album.getId());
                    sb.append(album.getId() + "$$" + album.getAlbumTitle() + ",");
                }
                LogUtils.d("分类、标签名下的专辑列表", list.size() + "$$" + sb.toString());
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    @Override
    public synchronized boolean play(String param) {
        isPlaying = true;
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.SEARCH_KEY, param);
        map.put(DTransferConstants.CATEGORY_ID, "2");
        map.put(DTransferConstants.PAGE, "1");
        map.put(DTransferConstants.CALC_DIMENSION, "1");
        /*ConnectivityManager mgrConn = (ConnectivityManager) BaseApplication.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mgrTel = (TelephonyManager) BaseApplication.mContext.getSystemService(Context.TELEPHONY_SERVICE);
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
                    mPlayerManager.playList(tracks, 0);
                }
            }

            @Override
            public void onError(int code, String message) {
                LogUtils.d("搜索声音", "code:" + code);
                notifyComplete();
            }
        });
        return true;
    }

    @Override
    public synchronized boolean joke() {
        // 分类列表：娱乐
        // 1--资讯,2--音乐,3--有声书,4--娱乐,6--儿童,7--健康养生,8--商业财经,9--历史人文,10--情感生活,
        // 11--其他,12--相声评书,13--教育培训,14--百家讲坛,15--广播剧,16--戏曲,17--电台,18--IT科技,
        // 21--汽车,22--旅游,23--电影,24--动漫游戏,28--脱口秀,29--3D体验馆,30--名校公开课,
        // 31--时尚生活,32--小语种,34--诗歌,38--英语

        // 标签列表：冷笑话、生活小趣
        // tag##糗事百科,tag##内涵段子,tag##节操精选,tag##80脱口秀,tag##星座不求人,tag##不吐槽会死,
        // tag##电视剧原声,tag##关爱八卦成长,tag##这里全是方言,tag##冷笑话,tag##经典电影原声,
        // tag##综艺玩很大,tag##超级访问,tag##明星粉丝电台,tag##万万没想到,

        // 专辑ID：听周星驰讲笑话
        // 455991$$黑历史,4728754$$听周星驰讲笑话,4022290$$笑不笑由你,4256322$$笑不笑由你 羞羞版,
        // 3475612$$杰讲笑话,4735586$$G点经济学,4782927$$欢乐正前方,4970855$$疯言丰语版十万个为什么之超短伪脱口秀,
        // 4907888$$兔大宝公开课,4977048$$娱乐时间,4932342$$小图特别篇,4879752$$臻好笑,4817620$$青舌日记,
        // 4839492$$【花絮集】无干货，非骨灰级粉丝慎入~~我笑点比较低。。,4566428$$笑话,3524159$$【每日一笑】,
        // 4078745$$段子搬运工,4218901$$哈哈逗你,4342792$$波波讲段子,3849250$$Alpha冷,4388970$$不止是幽默,
        // 4422113$$竹子讲笑话，羞羞哒,4322121$$生活小趣,4694948$$乐在其中,4660402$$晨彩飞扬,
        isPlaying = true;
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.ALBUM_ID, albumId.get(new Random().nextInt(albumId.size())) + "");
        map.put(DTransferConstants.SORT, "asc");
        map.put(DTransferConstants.PAGE, "1");//当前第几页，不填默认为1；以后可以改成随机
        CommonRequest.getTracks(map, new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                StringBuffer sb = new StringBuffer();
                List<Track> list = trackList.getTracks();
                for (Track track : list) {
                    sb.append(track.getDataId() + "**" + track.getTrackTitle() + ",");
                }
                LogUtils.d("专辑ID下的专辑列表", list.size() + "**" + sb.toString());

                Random random = new Random(0);
                int i = random.nextInt(list.size());
                tracks.clear();
                tracks.add(trackList.getTracks().get(i));
                mPlayerManager.clearPlayCache();
                mPlayerManager.playList(tracks, 0);
            }

            @Override
            public void onError(int i, String s) {
                LogUtils.d("专辑ID下的专辑列表", "获取失败:" + s);
                notifyComplete();
            }
        });
        return true;
    }

    private void notifyComplete() {
        isPlaying = false;
        if (listener != null)
            listener.onComplete();
    }

    @Override
    public boolean stop() {
        if (mPlayerManager != null) {
            mPlayerManager.stop();
        }
        return true;
    }

    @Override
    public void release() {
        if (mPlayerManager != null) {
            mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
            mPlayerManager.release();
        }
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void setListener(MusicListener listener) {
        this.listener = listener;
    }
}
