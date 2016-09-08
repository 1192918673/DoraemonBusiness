package com.geeknewbee.doraemon.input;

import android.view.SurfaceView;

import com.geeknewbee.doraemonsdk.utils.LogUtils;

/**
 * 眼睛
 * 用于实现人脸识别、拍照
 */
public interface IEye {

    /**
     * 开启ReadSence功能
     * @param preView
     */
    void startReadSence(SurfaceView preView);

    /**
     * 关闭ReadSence功能
     */
    void stopReadSence();

    /**
     * 开始添加人脸
     */
    void startAddFace();

    /**
     * 停止添加人脸
     */
    void stopAddFace();

    /**
     * 开始人脸识别
     */
    void startRecognition();

    /**
     * 停止人脸识别
     */
    void stopRecognition();

    /**
     * 开始拍照
     *
     * @param isAuto 是否是检测到人脸自动拍照
     */
    void startTakePicture(boolean isAuto);

    /**
     * 停止拍照
     */
    void stopTakePicture();
}
