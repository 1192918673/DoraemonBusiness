package com.youku.download;

import com.youku.player.VideoQuality;
import com.youku.player.util.PlayerUiUtile;

import java.util.List;

public class DownLoadManager {

    private IDownLoadDelegate downLoadDelegate;

    private DownLoadManager() {
        downLoadDelegate = PlayerUiUtile.getDownLoadDelegate();
//		downLoadDelegate=new DownLoadDelegate();
    }

    private static class SingletonHolder {// 构造线程安全的单例
        public final static DownLoadManager instance = new DownLoadManager();
    }

    public final static DownLoadManager getInstance() {
        return SingletonHolder.instance;
    }

    public boolean addNewDownLoad(String vid, String name, VideoQuality videoQuality) {
        // TODO Auto-generated method stub
        return downLoadDelegate.addNewDownLoad(vid, name, videoQuality);
    }

    public boolean deleteDownLoad(String vid) {
        // TODO Auto-generated method stub
        return downLoadDelegate.deleteDownLoad(vid);
    }

    public DownInfo getDownInfoByVid(String vid) {
        // TODO Auto-generated method stub
        return downLoadDelegate.getDownInfoByVid(vid);
    }

    public List<DownInfo> getDownInfos() {
        // TODO Auto-generated method stub
        return downLoadDelegate.getDownInfos();
    }

    public String getSaveAbsolutePath() {
        // TODO Auto-generated method stub
        return downLoadDelegate.getSaveAbsolutePath();
    }

    public boolean retryDownLoad(String vid) {
        // TODO Auto-generated method stub
        return downLoadDelegate.retryDownLoad(vid);
    }

    public void setDownLoaderListener(DownLoaderListener listener) {
        // TODO Auto-generated method stub
        downLoadDelegate.setDownLoaderListener(listener);
    }

    public void setSaveAbsolutePath(String vid) {
        // TODO Auto-generated method stub
        downLoadDelegate.setSaveAbsolutePath(vid);
    }

    public void setStartWhenPrepared(boolean arg0) {
        // TODO Auto-generated method stub
        downLoadDelegate.setStartWhenPrepared(arg0);
    }

    public boolean startDownLoad(String vid) {
        // TODO Auto-generated method stub
        return downLoadDelegate.startDownLoad(vid);
    }

    public boolean stopDownLoad(String vid) {
        // TODO Auto-generated method stub
        return downLoadDelegate.stopDownLoad(vid);
    }

}
