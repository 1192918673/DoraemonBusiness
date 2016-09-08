package com.geeknewbee.doraemon.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.facebook.stetho.common.LogUtil;
import com.geeknewbee.doraemon.input.ReadSenseEye;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.Iterator;
import java.util.List;

import dou.utils.DisplayUtil;

@SuppressWarnings("deprecation")
public class CameraHelper {

    private final String TAG = ReadSenseEye.TAG;
    private Camera.PreviewCallback previewCallback;
    private Camera camera = null;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder = null;
    private Context context;
    private Camera.Size previewSize;

    private int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

    private int sw, sh;
    private OnCameraStatusListener listener;
    // 创建一个PictureCallback对象，并实现其中的onPictureTaken方法
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        // 该方法用于处理拍摄后的照片数据
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 停止照片拍摄
            camera.stopPreview();

            // 调用结束事件
            if (null != listener) {
                listener.onCameraStopped(data);
            }
        }
    };

    public CameraHelper(Context context, SurfaceView surfaceView) {
        this.context = context;
        this.surfaceView = surfaceView;
        this.sw = DisplayUtil.getScreenWidthPixels(context);
        this.sh = DisplayUtil.getScreenHeightPixels(context);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                initCamera();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopCamera();
            }
        });

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {

                    }
                });
            }
        });
    }

    public void stopCamera() {
        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void openCamera() {
        try {
            if (hasFacing(cameraFacing)) {
                LogUtils.d(TAG, "有后置摄像头");
                camera = Camera.open(cameraFacing);
            } else {
                LogUtils.d(TAG, "没有后置摄像头");
                cameraFacing = (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT);
                camera = Camera.open(cameraFacing);
            }

            camera.setPreviewDisplay(surfaceHolder);
            LogUtils.d(TAG, "相机设置预览SurfaceHolder");
            initCamera();
        } catch (Exception e) {
            LogUtils.d(TAG, "打开相机异常。。。");
            e.printStackTrace();
            if (null != camera) {
                camera.release();
                camera = null;
            }
        }
    }

    private void initCamera() {
        if (null != camera) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                parameters.setPreviewFormat(ImageFormat.NV21);
                parameters.setPictureFormat(PixelFormat.JPEG);
//                parameters.set("jpeg-quality", 85);
                setOptimalPreviewSize(parameters, 960, 640);//phone
//                setOptimalPreviewSize(parameters, 640, 640);//pad

                if (context.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    LogUtils.d(TAG, "竖屏");
                    parameters.set("orientation", "portrait");
                    parameters.set("rotation", "90");
                    camera.setDisplayOrientation(90);
                } else {
                    LogUtils.d(TAG, "横屏");
                    parameters.set("orientation", "landscape");
                    parameters.set("rotation", "0");
                    camera.setDisplayOrientation(0);
                }
                camera.setParameters(parameters);
                camera.setPreviewCallback(previewCallback);
                camera.cancelAutoFocus();
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
        if (camera != null) camera.setPreviewCallback(previewCallback);
    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParams, int targetWidth, int targetHeight) {
        List<Camera.Size> supportedPreviewSizes = cameraParams.getSupportedPreviewSizes();

        if (null == supportedPreviewSizes) {
        } else {
//            Camera.Size optimalSize = null;
//            Iterator mIterator = supportedPreviewSizes.iterator();
//            int ssw = 1920;
//            while (mIterator.hasNext()) {//适合
//                Camera.Size size = (Camera.Size) mIterator.next();
//                if (sw / (float) sh == size.height / (float) size.width && size.width <= ssw) {
//                    optimalSize = size;
//                    ssw = optimalSize.width;
//                }
//            }

            Camera.Size optimalSize = null;
            double minDiff = 1.7976931348623157E308D;
            Iterator mIterator = supportedPreviewSizes.iterator();

            while (mIterator.hasNext()) {
                Camera.Size size = (Camera.Size) mIterator.next();
                if ((double) Math.abs(size.width - targetWidth) < minDiff) {
                    optimalSize = size;
                    minDiff = (double) Math.abs(size.width - targetWidth);
                }
            }

            assert optimalSize != null;
            setPreviewSize(optimalSize);
            int iw = optimalSize.width;
            int ih = optimalSize.height;

            LogUtils.d(TAG, iw + ":" + ih + ":" + sw);


            if (iw * sh <= ih * sw) {
                surfaceView.getLayoutParams().width = sh * iw / ih;
                surfaceView.getLayoutParams().height = sh;
            } else {
                surfaceView.getLayoutParams().width = sw;
                surfaceView.getLayoutParams().height = sw * iw / ih;
            }

            surfaceView.requestLayout();
            cameraParams.setPreviewSize(iw, ih);
            cameraParams.setPictureSize(640, 480);
        }
    }

    // 设置监听事件
    public void setOnCameraStatusListener(OnCameraStatusListener listener) {
        this.listener = listener;
    }

    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    public void setPreviewSize(Camera.Size previewSize) {
        this.previewSize = previewSize;
    }

    public int switchCameraId() {
        if (!hasFacing(Camera.CameraInfo.CAMERA_FACING_FRONT)) return cameraFacing;
        cameraFacing = (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT ?
                Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT);
        stopCamera();
        openCamera();
        return cameraFacing;

    }

    private boolean hasFacing(int facing) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);

            if (info.facing == facing) {
                return true;
            }
        }
        return false;
    }

    public int getCameraId() {
        return cameraFacing;
    }

    public void takePicture() {
        LogUtil.d(TAG, "判断Camera是否为空");
        if (camera != null) {
            LogUtil.d(TAG, "Camera不为空");
            camera.autoFocus(new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        LogUtils.d(TAG, "相机自动聚焦成功，开始拍照");
                        camera.takePicture(null, null, pictureCallback);
                    }
                }
            });
        }
    }

    public void reStartCamera() {
        camera.startPreview();
    }

    /**
     * 相机拍照监听接口
     */
    public interface OnCameraStatusListener {

        // 相机拍照结束事件
        void onCameraStopped(byte[] data);
    }
}
