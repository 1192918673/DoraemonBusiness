package com.geeknewbee.doraemon.input;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.TextureView;
import android.view.WindowManager;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.utils.PrefUtils;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.RetrofitCallBack;
import com.geeknewbee.doraemon.webservice.RetrofitHelper;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;

public class ReadSenseService extends Service implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    public static final String TAG = ReadSenseService.class.getSimpleName();
    public static final String PHOTOTYPE_AUTO = "1";
    public static final String PHOTOTYPE_HANDLE = "2";
    public static final int iw = 640;
    public static final int ih = 480;
    private static final int UPLOAD_PICTURE = 3;
    private static final int UPLOAD_SUCCESS = 4;
    private static final int UPLOAD_FAILED = 5;
    private boolean NEED_TAKE_PICTURE = false;
    private long LAST_TAKE_PICTURE_TIME;
    private int TAKE_PICTURE_INTERVAL = 30 * 1000; // 拍照时间戳半分钟
    private long TTS_PICTURE_START;
    private long TTS_PICTURE_INTERVAL = 2 * 1000; // TTS时间戳2秒
    private boolean busy = false; // 是否正在检测
    private TextureView previewTexture;
    private Camera mCamera;
    private YMFaceTrack faceTrack;
    private Bitmap bitmap;
    private Intent mIntent;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPLOAD_PICTURE:
                    uploadPicture(bitmap, msg.arg1 == 1);
                    break;
                case UPLOAD_SUCCESS:
                    LogUtils.d(TAG, "上传成功");
                    break;
                case UPLOAD_FAILED:
                    LogUtils.d(TAG, "上传失败");
                    break;
            }
        }
    };
    private ReadSenseReceiver mReadSenseReceiver;
    private PendingIntent pintent;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(ReadSenseService.TAG, "onCreate 调用。。。");
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new Notification(R.mipmap.launcher,
                "readSense service is running",
                System.currentTimeMillis());
        pintent = PendingIntent.getService(this, 0, intent, 0);
        notification.setLatestEventInfo(this, "ReadSense Service",
                "readSense service is running！", pintent);

        //让该service前台运行，避免手机休眠时系统自动杀掉该服务
        //如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground(999, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void init() {
        LogUtils.d(TAG, "init");

        // 1.创建TextureView对象，设置监听
        previewTexture = new TextureView(this);
        previewTexture.setSurfaceTextureListener(this);
        previewTexture.setKeepScreenOn(true);

        // 2.设置TextureView参数
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
        wm.addView(previewTexture, ballWmParams);// 添加显示

        // 3.初始化ReadFace
        initFaceTrack();

        // 4.初始化TIPS广播的Intent
        mIntent = new Intent(Constants.READSENSE_BROADCAST_TIPS_ACTION);

        // 5.注册切换拍照的广播
        mReadSenseReceiver = new ReadSenseReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.READSENSE_BROADCAST_TAKE_PICTURE_ACTION);
        intentFilter.addAction(Constants.ACTION_DORAEMON_REINIT_FACE_TRACK);
        registerReceiver(mReadSenseReceiver, intentFilter);
    }

    private void initFaceTrack() {
        faceTrack = new YMFaceTrack();
        faceTrack.initTrack(this, YMFaceTrack.FACE_180, YMFaceTrack.RESIZE_WIDTH_640);
        faceTrack.setRecognitionConfidence(80);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LogUtils.d(TAG, "onSurfaceTextureAvailable");

        // 4.打开相机：设置监听、设置参数、开启预览
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            configAndRelayout();
            startPreview();
            LogUtils.d(TAG, "open Camera success");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, "open Camera fail");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        LogUtils.d(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //LogUtils.d(TAG, "onSurfaceTextureUpdated");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LogUtils.d(TAG, "onSurfaceTextureDestroyed");
        stopPreview();
        return false;
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        LogUtils.d(TAG, "相机回调");
        if (!busy && faceTrack != null) {
            busy = true;

            LogUtils.d(TAG, "人脸识别。。。");
            List<YMFace> faces = faceTrack.trackMulti(data, iw, ih);
            if (NEED_TAKE_PICTURE) {
                NEED_TAKE_PICTURE = false;
                takePicture(data, false);
            }

            if (faces != null && faces.size() != 0) {
                LogUtils.d(TAG, "检测到人脸。。。");
                int person = faceTrack.identifyPerson(0);
                if (person != -111) {
                    Intent intent = new Intent(Constants.ACTION_DORAEMON_DISCOVERY_PERSON);
                    intent.putExtra(Constants.EXTRA_PERSON_ID, person);
                    sendBroadcast(intent);
                }
                LogUtils.d(TAG, "检测到具体人。。。" + person);
                takePicture(data, true);
            }
            busy = false;
        }
    }

    private void configAndRelayout() {
        if (mCamera == null)
            return;

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(iw, ih);
        parameters.set("rotation", "180");
        mCamera.setPreviewCallback(this);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(180);
    }

    private void startPreview() {
        LogUtils.d(TAG, "startPreview mCamera:" + (mCamera == null));

        if (mCamera == null)
            return;
        try {
            mCamera.setPreviewTexture(previewTexture.getSurfaceTexture());
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void takePicture(byte[] data, boolean isAutoTakePicture) {
        if (isAutoTakePicture) {
            // 1.如果是自动拍照，再判断距上次拍照是不是够半分钟
            if (System.currentTimeMillis() - LAST_TAKE_PICTURE_TIME > TAKE_PICTURE_INTERVAL) {
                LogUtils.d(TAG, "Auto take picture, 0.5-minute intervals time out, take picture now...");

                LAST_TAKE_PICTURE_TIME = System.currentTimeMillis();
                LogUtils.d(TAG, "偷拍了你一张照片。。。");
                TTS_PICTURE_START = System.currentTimeMillis();
                startTake(data, isAutoTakePicture);
            } else
                LogUtils.d(TAG, "拍照间隔未到。。。");
        } else {
            // 2.如果是命令拍照，直接调用拍照
            LogUtils.d(TAG, "Command take picture, take picture now...");

            TTS_PICTURE_START = System.currentTimeMillis();
            LogUtils.d(TAG, "主动拍了一张照片。。。");
            speak("照片拍好了");
            LAST_TAKE_PICTURE_TIME = System.currentTimeMillis();
            startTake(data, isAutoTakePicture);
        }
    }

    private void startTake(byte[] data, boolean isAutoTakePicture) {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        Rect rect = new Rect(0, 0, iw, ih);
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, iw, ih, null);
        yuvimage.compressToJpeg(rect, 100, outstr);
        Bitmap bmp = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());
        bitmap = rotaingImageView(180, bmp);

        mHandler.obtainMessage(UPLOAD_PICTURE, isAutoTakePicture ? 1 : 2, 0).sendToTarget();
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    private Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * 上传照片
     *
     * @param bitmap
     */
    private void uploadPicture(Bitmap bitmap, final boolean isAutoTakePicture) {
        LogUtils.d(TAG, "开始上传照片。。。");
        String authToken = PrefUtils.getString(this, Constants.KEY_TOKEN, Constants.EMPTY_STRING);
        File file = new File(Environment.getExternalStorageDirectory(), "robot_doraemon");
        if (!file.exists())
            file.mkdirs();
        File image = new File(file, "take_picture.jpg");
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestBody photo = RequestBody.create(MediaType.parse("image"), image);
        RequestBody token = RequestBody.create(MediaType.parse("text/plain"), authToken);
        RequestBody photoType = RequestBody.create(MediaType.parse("text/plain"), isAutoTakePicture ? PHOTOTYPE_AUTO : PHOTOTYPE_HANDLE);

        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        ApiService service = retrofit.create(ApiService.class);
        LogUtils.d(TAG, "Token :" + authToken + "; PhotoType :" + photoType + "; Photo :" + photo);
        RetrofitHelper.sendRequest(service.uploadPhoto(token, photoType, photo), new RetrofitCallBack<Object>() {
            @Override
            public void onSuccess(Object response) {
                LogUtils.d(TAG, "Upload picture success");
                mHandler.sendEmptyMessage(UPLOAD_SUCCESS);
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(TAG, "Upload picture error :" + error);
                mHandler.sendEmptyMessage(UPLOAD_FAILED);
            }
        });
    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void speak(String text) {
        mIntent.putExtra("text", text);
        sendBroadcast(mIntent);
        LogUtils.d(TAG, "发送Tips广播。。。");
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "onDestroy 调用。。。");
        stopPreview();
        faceTrack.onRelease();
        unregisterReceiver(mReadSenseReceiver);
        stopForeground(true);
        super.onDestroy();
    }

    class ReadSenseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.READSENSE_BROADCAST_TAKE_PICTURE_ACTION:
                    LogUtils.d(TAG, "收到拍照的命令广播。。。");
                    NEED_TAKE_PICTURE = true;
                    break;
                case Constants.ACTION_DORAEMON_REINIT_FACE_TRACK:
                    LogUtils.d(TAG, "收到ReInit faceTrack");
                    faceTrack.onRelease();
                    faceTrack = null;
                    initFaceTrack();
                    break;
            }
        }
    }
}
