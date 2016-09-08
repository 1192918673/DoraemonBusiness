package com.geeknewbee.doraemon.input;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import dou.utils.DisplayUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Retrofit;

/**
 * ReadSense 实现 Eye
 */
public class ReadSenseEye implements IEye, CameraHelper.OnCameraStatusListener, Camera.PreviewCallback {

    public static final String TAG = ReadSenseEye.class.getSimpleName();
    public static final String PHOTOTYPE_AUTO = "1";
    public static final String PHOTOTYPE_HANDLE = "2";
    private static final int ADD_FACE = 0; // 添加人脸
    private static final int REC_FACE = 1; // 人脸识别
    private static final int PHO_FACE = 2; // 拍照
    private static final int FUN_GO = PHO_FACE; // 微笑拍照
    private volatile static ReadSenseEye instance;
    private static boolean mIsAutoPicture; // 是否是自动拍照，还是口令让他拍照
    protected YMFaceTrack faceTrack;
    private Context context = BaseApplication.mContext;
    private SurfaceView camera_view; // 相机预览
    private boolean busy = false; // 是否正在检测
    private CameraHelper mCameraHelper;
    private int iw = 0, ih = 0; // 相机预览图像的宽高
    private int sw, sh; // 屏幕宽高
    private float scale_bit = 0; // 指图像大小放大到屏幕指定尺寸的比率（无用）
    private long lastTakePictureTime;

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
        sw = DisplayUtil.getScreenWidthPixels(context);
        sh = DisplayUtil.getScreenHeightPixels(context);

        mCameraHelper = new CameraHelper(context, camera_view);
        mCameraHelper.setOnCameraStatusListener(this); // 相机的状态监听
        mCameraHelper.setPreviewCallback(this); // 相机的预览监听

        faceTrack = new YMFaceTrack();
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640); // 猫的相机：横屏前置
        faceTrack.setRecognitionConfidence(75);
        busy = false;
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

    }

    @Override
    public void stopAddFace() {

    }

    @Override
    public void startRecognition() {

    }

    @Override
    public void stopRecognition() {

    }

    @Override
    public void startTakePicture(boolean isAuto) {
        LogUtils.d(TAG, "Start takePicture...");

        mIsAutoPicture = isAuto;
        if (isAuto) {
            tips("偷拍了你一张照片");
        } else {
            tips("好的");
        }
        if (mCameraHelper != null) {
            mCameraHelper.takePicture();
        }
    }

    @Override
    public void stopTakePicture() {

    }

    /**
     * 相机的预览监听
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!busy) {
            iw = camera.getParameters().getPreviewSize().width;
            ih = camera.getParameters().getPreviewSize().height;
            if (scale_bit == 0) scale_bit = sw / (float) ih;
            busy = true;

            switch (FUN_GO) {
                case PHO_FACE: // 拍照
                    trackFaces(data, iw, ih);
                    break;
            }
        }
    }

    private void trackFaces(byte[] bytes, int iw, int ih) {
        List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);

        // 1.检测到人脸
        if (faces != null && faces.size() != 0) {

            // 2.有人脸再判断距上次拍照是不是够十秒
            if (System.currentTimeMillis() - lastTakePictureTime > 10000) {
                LogUtils.d(TAG, "Tracked Faces 10 Time...");
                lastTakePictureTime = System.currentTimeMillis();
                startTakePicture(true);
            } else {
                busy = false;
                return;
            }
        } else {
            busy = false;
            return;
        }
    }

    private void tips(String text) {
        Doraemon.getInstance(context).addCommand(new SoundCommand(text, SoundCommand.InputSource.TIPS));
    }

    /**
     * 相机的状态监听(照片拍完)
     *
     * @param data
     */
    @Override
    public void onCameraStopped(byte[] data) {
        LogUtils.d(TAG, "Take picture completed...");

        Matrix matrix = new Matrix();
        matrix.reset();
        if (mCameraHelper.getCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            matrix.postRotate(180);
            matrix.postScale(-1, 1);
        }

        // 4.保存照片 or 上传到服务器
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        /*boolean saveSuccessed = BitmapUtil.saveBitmap(bitmap, "/mnt/sdcard/test.jpg");
        LogUtils.d(TAG, saveSuccessed ? "Save success" : "Save success");*/
        busy = false;
        mCameraHelper.reStartCamera();
        mCameraHelper.setPreviewCallback(this);

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
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(TAG, "Upload picture error :" + error);
            }
        });
    }
}
