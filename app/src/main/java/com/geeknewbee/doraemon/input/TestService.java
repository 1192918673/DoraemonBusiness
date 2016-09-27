package com.geeknewbee.doraemon.input;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.view.WindowManager;

import java.io.IOException;

public class TestService extends Service implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    public static final String TAG = TestService.class.getSimpleName();
    private TextureView previewTexture;
    private Camera mCamera;
    public static final int PREVIEWIWDTH = 640;
    public static final int PREVIEWHEIGHT = 480;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createView();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void createView() {
        Log.d(TAG, "createView");

        previewTexture = new TextureView(this);
        previewTexture.setSurfaceTextureListener(this);
        previewTexture.setKeepScreenOn(true);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams ballWmParams = new WindowManager.LayoutParams();
        ballWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        ballWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        ballWmParams.gravity = Gravity.LEFT | Gravity.TOP;
        ballWmParams.x = 0;
        ballWmParams.y = 0;
        ballWmParams.width = 1;
        ballWmParams.height = 1;
        ballWmParams.format = PixelFormat.RGBA_8888;
// 添加显示
        wm.addView(previewTexture, ballWmParams);
    }

    private void configAndRelayout() {
        if (mCamera == null)
            return;

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(PREVIEWIWDTH, PREVIEWHEIGHT);
        mCamera.setPreviewCallback(this);
        mCamera.setParameters(parameters);

//        int screentWidth = getWindowManager().getDefaultDisplay().getWidth();
//        int screentHeight = getWindowManager().getDefaultDisplay().getHeight();
//        float scale = Math.min(screentWidth / (float) PREVIEWIWDTH, screentHeight / (float) PREVIEWHEIGHT);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) (PREVIEWIWDTH * scale), (int) (PREVIEWHEIGHT * scale));
//        previewTexture.setLayoutParams(layoutParams);
    }


    private void startPreview() {
        Log.d(TAG, "startPreview mCamera:" + (mCamera == null));


        if (mCamera == null)
            return;
        try {
            mCamera.setPreviewTexture(previewTexture.getSurfaceTexture());
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(TAG, "onPreviewFrame");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        configAndRelayout();
        startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        stopPreview();
        return false;
    }

    private void stopPreview() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopPreview();
        super.onDestroy();
    }
}
