package com.geeknewbee.doraemon.output;

import android.content.Context;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.ReadFaceInitParams;
import com.geeknewbee.doraemon.input.bluetooth.BluetoothServiceManager;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * 处理添加人脸功能
 */
public class ReadFace {
    public static final int INCOGNIZANT = -111;
    public static final String TAG = ReadFace.class.getSimpleName();

    public static volatile ReadFace instance;
    private final Context context;
    private List<byte[]> faces;
    private Map<String, String> persons;
    private YMFaceTrack faceTrack;
    private int iw;
    private int ih;

    private ReadFace(Context context) {
        faces = new ArrayList<>();
        persons = new HashMap<>();
        this.context = context;
        //TODO 初始化persons 数据 from DB
    }

    public static ReadFace getInstance(Context context) {
        if (instance == null) {
            synchronized (ReadFace.class) {
                if (instance == null) {
                    instance = new ReadFace(context);
                }
            }
        }
        return instance;
    }

    public void addCommand(Command command) {
        String data;
        boolean b = false;
        switch (command.getType()) {
            case PERSON_START:
                Gson gson = new Gson();
                ReadFaceInitParams initParams = gson.fromJson(command.getContent(), ReadFaceInitParams.class);
                b = startAddFace(initParams.orientation, initParams.resizeScale, initParams.iw, initParams.ih);
                data = getCallbackString(b, BluetoothServiceManager.TYPE_PERSON_START);
                BluetoothServiceManager.getInstance(context).writeToSocket(data);
                break;
            case PERSON_ADD_FACE:
                try {
                    b = addFace(command.getContent().getBytes("ASCII"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                data = getCallbackString(b, BluetoothServiceManager.TYPE_PERSON_ADD_FACE);
                BluetoothServiceManager.getInstance(context).writeToSocket(data);
                break;
            case PERSON_SET_NAME:
                String content = command.getContent();
                try {
                    content = new String(content.getBytes("ASCII"), "ASCII");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                b = setPersonName(content);
                data = getCallbackString(b, BluetoothServiceManager.TYPE_PERSON_SET_NAME);
                BluetoothServiceManager.getInstance(context).writeToSocket(data);
                break;
        }
    }

    private String getCallbackString(boolean b, int type) {
        String data = Constants.EMPTY_STRING;
        data += Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET;
        data += type;
        data += (b ? 1 : 0);
        data += Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET;
        LogUtils.d(TAG, "call back data:" + data);
        return data;
    }

    /**
     * 根据personID获取人名
     *
     * @param personID
     * @return
     */
    public String getPersonName(String personID) {
        return persons.get(personID);
    }

    /**
     * 开始添加人脸
     *
     * @param orientation 摄像头方向
     * @param resizeScale resizeScale
     * @param iw          预览宽度
     * @param ih          预览高度
     * @return
     */
    private boolean startAddFace(int orientation, int resizeScale, int iw, int ih) {
        LogUtils.d(TAG, "startAddFace: orientation:" + orientation + " resizeScale:" + resizeScale + " iw:" + iw + " ih:" + ih);
        if (faceTrack != null) {
            faceTrack.onRelease();
            faceTrack = null;
        }
        faces.clear();
        faceTrack = new YMFaceTrack();
        //此处默认初始化，initCameraMsg()处会根据设备设置自动更改设置
        faceTrack.initTrack(context, orientation, resizeScale);
        faceTrack.setRecognitionConfidence(80);
        this.iw = iw;
        this.ih = ih;
        return true;
    }

    /**
     * 添加人脸数据
     *
     * @param bytes
     * @return
     */
    private boolean addFace(byte[] bytes) {
        LogUtils.d(TAG, "addFace");

        if (faceTrack == null)
            return false;

        YMFace face = faceTrack.track(bytes, iw, ih);
        if (face != null)
            faces.add(bytes);
        return face != null;
    }

    /**
     * 设置人脸对应的人名
     *
     * @param name
     * @return
     */
    private boolean setPersonName(String name) {
        LogUtils.d(TAG, "setPersonName:" + name);

        if (faces.isEmpty() || faceTrack == null) return false;

        faceTrack.track(faces.get(0), iw, ih);
        int personId = faceTrack.identifyPerson(0);
        if (personId != INCOGNIZANT) {
            //说明已经添加过，则覆盖
            faceTrack.deletePerson(personId);
            persons.remove(String.valueOf(personId));
            //TODO 删除 数据 from DB
        }
        personId = faceTrack.addPerson(0);
        if (personId == INCOGNIZANT)
            return false;

        for (int i = 1; i < faces.size(); i++) {
            faceTrack.track(faces.get(i), iw, ih);
            faceTrack.updatePerson(personId, 0);
        }
        faces.clear();
        persons.put(String.valueOf(personId), name);
        faceTrack.onRelease();
        faceTrack = null;
        //TODO 保存 数据 to DB
        return true;
    }
}
