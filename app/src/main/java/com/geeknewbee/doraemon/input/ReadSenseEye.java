package com.geeknewbee.doraemon.input;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.utils.CameraHelper;
import com.geeknewbee.doraemon.utils.PrefUtils;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.RetrofitCallBack;
import com.geeknewbee.doraemon.webservice.RetrofitHelper;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mobile.ReadFace.YMFaceTrack;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;

/**
 * ReadSense 实现 Eye
 */
public class ReadSenseEye implements IEye, Camera.PreviewCallback {

    public static final String TAG = ReadSenseEye.class.getSimpleName();
    public static final String PHOTOTYPE_AUTO = "1";
    public static final String PHOTOTYPE_HANDLE = "2";
    private static final int ADD_FACE = 0; // 添加人脸
    private static final int REC_FACE = 1; // 人脸识别
    private static final int PHO_FACE = 2; // 拍照
    private static final int UPLOAD_SUCCESS = 3;
    private static final int UPLOAD_FAILED = 4;
    private static int FUN_GO = PHO_FACE; // 微笑拍照
    private volatile static ReadSenseEye instance;
    private static boolean mIsAutoPicture; // 是否是自动拍照，还是口令让他拍照
    protected YMFaceTrack faceTrack;
    private Context context = BaseApplication.mContext;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private SurfaceView camera_view; // 相机预览
    private boolean busy = false; // 是否正在检测
    private CameraHelper mCameraHelper;
    private int iw = 0, ih = 0; // 相机预览图像的宽高
    private boolean needTakePicture = false;
    private long lastTakePictureTime;
    private int TAKE_PICTURE_INTERVAL = 60 * 1000;
    private long TIPS_START_TIME;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPLOAD_SUCCESS:
                    tips("拍好了");
                    break;
                case UPLOAD_FAILED:
                    tips("拍好了，上传失败");
                    break;
            }
        }
    };

    public static ReadSenseEye getInstance() {
        if (instance == null) {
            synchronized (ReadSenseEye.class) {
                if (instance == null) {
                    instance = new ReadSenseEye();
                }
            }
        }
        return instance;
    }

    private void init(SurfaceView preView) {
        camera_view = preView;
        /*camera_view = new SurfaceView(context);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) camera_view.getLayoutParams();
        params.width = 640;
        params.height = 640;
        camera_view.setLayoutParams(params);*/

        mCameraHelper = new CameraHelper(context, camera_view);
        mCameraHelper.setPreviewCallback(this); // 相机的预览监听

        /*faceTrack = new YMFaceTrack();
        faceTrack.initTrack(context, YMFaceTrack.FACE_180, YMFaceTrack.RESIZE_WIDTH_640); // 猫的相机：后置横屏
        faceTrack.setRecognitionConfidence(75);*/

        busy = false;
        startTakePicture(false);
    }

    @Override
    public void startReadSence(SurfaceView preView) {
        LogUtils.d(TAG, "Start ReadSence...");
        init(preView);
    }

    @Override
    public void stopReadSence() {
        LogUtils.d(TAG, "Stop ReadSence...");
        if (mCameraHelper != null) mCameraHelper.stopCamera();
        if (faceTrack != null) faceTrack.onRelease();
    }

    @Override
    public void startAddFace() {
        LogUtils.d(TAG, "Start Add Face...");

        FUN_GO = ADD_FACE;
    }

    @Override
    public void stopAddFace() {
        LogUtils.d(TAG, "Stop Add Face...");

        FUN_GO = PHO_FACE;
    }

    @Override
    public void startRecognition() {
        LogUtils.d(TAG, "Start Recognition...");

        FUN_GO = REC_FACE;
    }

    @Override
    public void stopRecognition() {
        LogUtils.d(TAG, "Stop Recognition...");

        FUN_GO = PHO_FACE;
    }

    @Override
    public void startTakePicture(boolean isAuto) {
        LogUtils.d(TAG, "Start takePicture...");

        needTakePicture = true; // 需要拍照
        mIsAutoPicture = isAuto;
    }

    @Override
    public void stopTakePicture() {
        if (mCameraHelper != null)
            mCameraHelper.stopCamera();
        if (faceTrack != null)
            faceTrack.onRelease();
    }

    /**
     * 相机的预览监听
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (!busy) {
            LogUtils.d(TAG, "相机回调");
            iw = camera.getParameters().getPreviewSize().width;
            ih = camera.getParameters().getPreviewSize().height;
            busy = true;

            switch (FUN_GO) {
                case PHO_FACE: // 拍照
                    trackFacesTP(data, iw, ih);
                    break;
            }
        }
    }

    private void trackFacesTP(final byte[] data, final int iw, final int ih) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                /*long start = System.currentTimeMillis();
                LogUtils.d(TAG, "开始检测人脸。。。" + start);*/
//                List<YMFace> faces = faceTrack.trackMulti(data, iw, ih);
                /*long end = System.currentTimeMillis();
                LogUtils.d(TAG, "结束检测人脸。。。" + end);
                LogUtils.d(TAG, end - start + "");*/


                // 1.需要拍照或检测到人脸 (needTakePicture || (faces != null && faces.size() != 0))
                if (needTakePicture) {
                    // 3.如果是自动拍照，再判断距上次拍照是不是够十秒
                    if (mIsAutoPicture) {
                        if (System.currentTimeMillis() - lastTakePictureTime > TAKE_PICTURE_INTERVAL) {
                            LogUtils.d(TAG, "Auto take picture, 1-minute intervals time out, take picture now...");

                            lastTakePictureTime = System.currentTimeMillis();
                            tips("偷拍了你一张照片");
                        }
                    } else {
                        LogUtils.d(TAG, "Command take picture, take picture now...");

//                        tips("好的 3 2 1");
                    }
                    takePicture(data, iw, ih);
                }
            }
        });
    }

    /**
     * 拍照
     *
     * @param data
     * @param iw
     * @param ih
     */
    private void takePicture(byte[] data, int iw, int ih) {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        Rect rect = new Rect(0, 0, iw, ih);
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, iw, ih, null);
        yuvimage.compressToJpeg(rect, 100, outstr);
        Bitmap bmp = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());
        Bitmap bitmap = rotaingImageView(180, bmp);

        busy = false;
        needTakePicture = false;
        mIsAutoPicture = true;
        TIPS_START_TIME = System.currentTimeMillis();
        uploadPicture(bitmap);
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
    private void uploadPicture(Bitmap bitmap) {
        String authToken = PrefUtils.getString(context, Constants.KEY_TOKEN, Constants.EMPTY_STRING);
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
        RequestBody photoType = RequestBody.create(MediaType.parse("text/plain"), mIsAutoPicture ? PHOTOTYPE_AUTO : PHOTOTYPE_HANDLE);

        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        ApiService service = retrofit.create(ApiService.class);
        LogUtils.d(TAG, "Token :" + authToken + "; PhotoType :" + photoType + "; Photo :" + photo);
        RetrofitHelper.sendRequest(service.uploadPhoto(token, photoType, photo), new RetrofitCallBack<Object>() {
            @Override
            public void onSuccess(Object response) {
                LogUtils.d(TAG, "Upload picture success");
                stopTakePicture();
                mHandler.sendEmptyMessageDelayed(UPLOAD_SUCCESS, 3 * 1000 - (System.currentTimeMillis() - TIPS_START_TIME));
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(TAG, "Upload picture error :" + error);
                stopTakePicture();
                mHandler.sendEmptyMessageDelayed(UPLOAD_FAILED, 3 * 1000 - (System.currentTimeMillis() - TIPS_START_TIME));
            }
        });
    }

    private void tips(String text) {
        Doraemon.getInstance(context).addCommand(new SoundCommand(text, SoundCommand.InputSource.TIPS));
    }
}