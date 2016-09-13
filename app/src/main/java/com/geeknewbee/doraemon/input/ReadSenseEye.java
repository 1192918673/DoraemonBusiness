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
import android.view.SurfaceView;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.bean.User;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.utils.CameraHelper;
import com.geeknewbee.doraemon.utils.DataSource;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dou.utils.Check;
import dou.utils.DisplayUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceAttribute;
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
    private static int FUN_GO = PHO_FACE; // 微笑拍照
    private volatile static ReadSenseEye instance;
    private static boolean mIsAutoPicture; // 是否是自动拍照，还是口令让他拍照
    protected YMFaceTrack faceTrack;
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private String[] emoTitle = {"喜悦", "悲愤", "傻逼", "傻逼", "惊讶", "愤怒", "正常"};
    private String[] names = {"刘德华", "刘德华", "刘德华", "刘德华", "刘德华", "刘德华", "刘德华", "刘德华", "刘德华", "刘德华"};
    private List<Float> save_list = new ArrayList<>();
    private Step step;
    private DataSource datasource;
    private int nullFrameCount = 0;
    private int anaISWhoCount = 0;
    private long startRecognition;
    private Context context = BaseApplication.mContext;
    private SurfaceView camera_view; // 相机预览
    private boolean busy = false; // 是否正在检测
    private CameraHelper mCameraHelper;
    private int iw = 0, ih = 0; // 相机预览图像的宽高
    private int sw, sh; // 屏幕宽高
    private float scale_bit = 0; // 指图像大小放大到屏幕指定尺寸的比率（无用）
    private boolean needTakePicture = false;
    private long lastTakePictureTime;
    private int TAKE_PICTURE_INTERVAL = 60 * 1000;
    private int track_id = -1;
    private int nowPersonId;

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

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        // 旋转图片动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    private void init(SurfaceView preView) {
        camera_view = preView;
        sw = DisplayUtil.getScreenWidthPixels(context);
        sh = DisplayUtil.getScreenHeightPixels(context);

        mCameraHelper = new CameraHelper(context, camera_view);
        mCameraHelper.setPreviewCallback(this); // 相机的预览监听

        faceTrack = new YMFaceTrack();
        faceTrack.initTrack(context, YMFaceTrack.FACE_180, YMFaceTrack.RESIZE_WIDTH_640); // 猫的相机：后置横屏
        faceTrack.setRecognitionConfidence(75);

        datasource = new DataSource(context);
        datasource.open();
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

    }

    /**
     * 相机的预览监听
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        performCallback(data, camera);
    }

    public void performCallback(final byte[] data, final Camera camera) {
        /*for (int i = 0; i < 10; i++) {
            final int index = i;
            try {
                Thread.sleep(500); // 休眠时间越短创建的线程数越多
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                LogUtils.d(TAG, "active count = " + Thread.activeCount());
                if (!busy) {
                    iw = camera.getParameters().getPreviewSize().width;
                    ih = camera.getParameters().getPreviewSize().height;
                    if (scale_bit == 0) scale_bit = sw / (float) ih;
                    busy = true;

                    switch (FUN_GO) {
                        case PHO_FACE: // 拍照
                            trackFacesTP(data, iw, ih);
                            break;
                            /*case REC_FACE: // 人脸识别
                                trackFacesRec(data, iw, ih);
                                break;
                            case ADD_FACE: // 人脸添加
                                trackFacesRec(data, iw, ih);
                                break;*/
                    }
                }
            }
        });
    }

    private void trackFacesTP(byte[] data, int iw, int ih) {
        List<YMFace> faces = faceTrack.trackMulti(data, iw, ih);

        // 1.检测到人脸
        if (faces != null && faces.size() != 0) {

            // 2.是否需要拍照
            if (needTakePicture) {

                // 3.如果是自动拍照，再判断距上次拍照是不是够十秒
                if (mIsAutoPicture) {
                    if (System.currentTimeMillis() - lastTakePictureTime > TAKE_PICTURE_INTERVAL) {
                        LogUtils.d(TAG, "Auto take picture, 1-minute intervals time out, take picture now...");

                        lastTakePictureTime = System.currentTimeMillis();
                        tips("偷拍了你一张照片");
                    } else {
                        LogUtils.d(TAG, "Command take picture, take picture now...");

                        tips("好的");
                    }
                    takePicture(data, iw, ih);
                }
            }
        }
        busy = false;
    }

    private void trackFacesRec(byte[] data, int iw, int ih) {
        tips("让我看看");
        startRecognition = System.currentTimeMillis();
        List<YMFace> faces = new ArrayList<>();
        YMFace singleFace = faceTrack.track(data, iw, ih);
        if (singleFace != null) {
            faces.add(singleFace);
            nullFrameCount = 0;

            if (track_id == -1) track_id = faces.get(0).getTrackId();
            if (track_id != faces.get(0).getTrackId()) {
                // 人脸更换
                busy = false;
                goFrist();
                nullFrameCount = 0;
                track_id = faces.get(0).getTrackId();
                return;
            }

            switch (step) {
                case step1: // 让我好好看看你
                    anaIsWho(faces);
                    break;
                case step5: // 添加正脸
                    if (isAdd1(faces)) {////顺便检测出属性顺便检测出属性
                        YMFaceAttribute attr = faceTrack.onDetectorAttribute(data, iw, ih, faces.get(0).getRect());
                        float[] genders = attr.getGender();
                        int age = (int) attr.getAge();
                        String gender = genders[0] > genders[1] ? "0" : "1";
                        if (nowPersonId != -111) {

                            String name = names[nowPersonId % names.length] + (genders[0] > genders[1] ? "小姐 " : "先生 ");
                            datasource.insert(new User(String.valueOf(nowPersonId), name, String.valueOf(age), gender));
                        }
                        step = Step.step6;
                    }
                    break;
                case step6: // 添加侧脸
                    if (isAdd2(faces)) {
                        step = Step.step7;
                    }
                    break;
                case step7: // 你是***岁的美女对吗 表情：喜悦

                    break;
            }
        } else {
            if (System.currentTimeMillis() - startRecognition > 2 * 60 * 1000) {
                tips("人脸都没看到，认识个屁啊");
                busy = false;
                return;
            }
        }
    }

    private void goFrist() {
        step = Step.step1;
        tips("来让我看看你是谁");
        if (faceTrack.getAlbumSize() >= 20) {
            faceTrack.resetAlbum();
            datasource.clearTable();
        }
    }

    private void anaIsWho(List<YMFace> faces) {
        anaISWhoCount++;
        if (anaISWhoCount >= 15) {
            if (faceTrack.getAlbumSize() >= 20) {
                // 人脸数据最多20
                faceTrack.resetAlbum();
                datasource.clearTable();
            }
            anaISWhoCount = 0;

            step = Step.step2; // 认识--> step3    不认识--> step4
            int personId = faceTrack.identifyPerson(0);
            if (personId == -111) {
                step = Step.step4; // 不认识
                tips("看不清哦，给我一个正脸看一下");
                step = FUN_GO == REC_FACE ? Step.step1 : Step.step5; // 返回step ：添加正脸
            } else {
                step = Step.step3; // 认识
                User user = null;
                try {
                    user = datasource.getUserByPersonId(String.valueOf(personId));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (user == null) {
                    faceTrack.deletePerson(personId);
                    step = Step.step1; // 数据库查不到，重新识别
                } else {
                    showKnowing(user, getMaxEmo(faces.get(0).getEmotions()));
                }
            }
        }
    }

    private String getMaxEmo(float[] emos) {
        int iemos[] = new int[emos.length];
        int max = 0;
        int position = 0;
        for (int i = 0; i < emos.length; i++) {
            iemos[i] = (int) (emos[i] * 100);
            if (max <= iemos[i]) {
                max = iemos[i];
                position = i;
            }
        }
        int now = position == 5 ? 1 : position;
        return emoTitle[now];
    }

    private void showKnowing(User user, String emo) {
        if (Check.isEmpty(user.getName()) || Check.isEmpty(String.valueOf(user.getAge()))) {
            faceTrack.resetAlbum();
            datasource.clearTable();
            goFrist();
            return;
        }
        tips("认识你：你是" + user.getName() + "  " + user.getAge() + "岁，" + "表情是：" + emo);
    }

    private void trackFacesAdd(byte[] data, int iw, int ih) {

    }

    private boolean isAdd1(List<YMFace> faces) {
        if (faces == null || faces.size() != 1) {
            tips("未检测到人脸或者人脸多于一个");
            return false;
        }

        YMFace face = faces.get(0);

        tips("请正脸面对");
        float facialOri[] = face.getHeadpose();

        float x = facialOri[0];
        float y = facialOri[1];
        float z = facialOri[2];
        //头部偏转为正脸时，记录下当前正脸
        if (Math.abs(x) <= 15 && Math.abs(y) <= 15 && Math.abs(z) <= 15) {
            nowPersonId = faceTrack.addPerson(0);
            if (faceTrack.getAlbumSize() >= 20) {
                faceTrack.resetAlbum();
                datasource.clearTable();
            }
            return true;
        }
        return false;
    }

    private boolean isAdd2(List<YMFace> faces) {//加侧脸数据
        if (faces == null || faces.size() != 1) {
            tips("未检测到人脸或者人脸多于一个");
            return false;
        }
        YMFace face = faces.get(0);

        tips("请侧脸");
        float z = face.getHeadpose()[2];

        save_list.add(z);
        if (save_list.size() >= 10) save_list.remove(0);

        if (!zIsStable()) {
            tips("角度偏移过快");
            return false;
        }

        if (Math.abs(z) >= 25) {
            faceTrack.updatePerson(nowPersonId, 0);
            User user = null;
            try {
                user = datasource.getUserByPersonId(String.valueOf(nowPersonId));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (user == null) {
                faceTrack.deletePerson(nowPersonId);
                step = (FUN_GO == REC_FACE ? Step.step1 : Step.step5); // 返回step ：添加正脸
            } else {
                showKnowing(user, getMaxEmo(faces.get(0).getEmotions()));
            }
            return true;
        }
        return false;
    }

    boolean zIsStable() {
        int size = save_list.size();
        if (size >= 5) {
            if (Math.abs(save_list.get(size - 1) - save_list.get(size - 2)) <= 10 &&
                    Math.abs(save_list.get(size - 2) - save_list.get(size - 3)) <= 10) {
                return true;
            }
        }
        return false;
    }

    /**
     * 拍照
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

        mIsAutoPicture = true;
        uploadPicture(bitmap);
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
            }

            @Override
            public void onFailure(String error) {
                LogUtils.d(TAG, "Upload picture error :" + error);
            }
        });
    }

    private void tips(String text) {
        Doraemon.getInstance(context).addCommand(new SoundCommand(text, SoundCommand.InputSource.TIPS));
    }

    enum Step {
        step1, step2, step3, step4, step5,step6,step7
    }
}
