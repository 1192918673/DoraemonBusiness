package com.geeknewbee.doraemon.output;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.database.Person;
import com.geeknewbee.doraemon.database.PersonDao;
import com.geeknewbee.doraemon.entity.ReadFaceInitParams;
import com.geeknewbee.doraemon.entity.event.FaceControlCompleteEvent;
import com.geeknewbee.doraemon.input.bluetooth.WirelessControlServiceManager;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.command.AddFaceCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * 处理添加人脸功能
 */
public class ReadFaceManager implements IOutput {
    public static final int INCOGNIZANT = -111;
    public static final String TAG = ReadFaceManager.class.getSimpleName();

    public static volatile ReadFaceManager instance;
    private final Context context;
    private final ExecutorService executorService;
    private List<Face> facesNew;
    private Map<Integer, String> persons;
    private YMFaceTrack faceTrack;
    private int iw;
    private int ih;
    private Command activeCommand;

    private ReadFaceManager(Context context) {
        facesNew = new ArrayList<>();
        persons = new HashMap<>();
        this.context = context;
        List<Person> list = App.instance.getDaoSession().getPersonDao().queryBuilder().list();
        for (Person p : list) {
            persons.put(p.getPersonId(), p.getName());
        }
        executorService = Executors.newSingleThreadExecutor();
    }

    public static ReadFaceManager getInstance(Context context) {
        if (instance == null) {
            synchronized (ReadFaceManager.class) {
                if (instance == null) {
                    instance = new ReadFaceManager(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void addCommand(final Command command) {
        switch (command.getType()) {
            case PERSON_START:
                activeCommand = command;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        ReadFaceInitParams initParams = gson.fromJson(command.getContent(), ReadFaceInitParams.class);
                        boolean b = startAddFace(initParams.orientation, initParams.resizeScale, initParams.iw, initParams.ih);
                        String data = getCallbackString(b, WirelessControlServiceManager.TYPE_PERSON_START);
                        WirelessControlServiceManager.getInstance(context).writeToSocket(data);
                        notifyComplete();
                    }
                });
                break;
            case PERSON_ADD_FACE:
                activeCommand = command;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        String data = "";
                        AddFaceCommand faceCommand = (AddFaceCommand) command;
                        boolean b = addFace(faceCommand);
                        if (faceCommand.faceType == AddFaceType.YUV)
                            data = getCallbackString(b, WirelessControlServiceManager.TYPE_PERSON_ADD_FACE);
                        else if (faceCommand.faceType == AddFaceType.IMAGE)
                            data = getCallbackString(b, WirelessControlServiceManager.TYPE_PERSON_ADD_FACE_IMAGE);
                        WirelessControlServiceManager.getInstance(context).writeToSocket(data);
                        notifyComplete();
                    }
                });
                break;
            case PERSON_SET_NAME:
                activeCommand = command;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        String content = command.getContent();
                        boolean b = setPersonName(content);
                        String data = getCallbackString(b, WirelessControlServiceManager.TYPE_PERSON_SET_NAME);
                        WirelessControlServiceManager.getInstance(context).writeToSocket(data);
                        notifyComplete();
                    }
                });
                break;
            case PERSON_DELETE_ALL:
                activeCommand = command;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        ReadFaceInitParams initParams2 = gson.fromJson(command.getContent(), ReadFaceInitParams.class);
                        deleteAddPerson(initParams2.orientation, initParams2.resizeScale);
                        notifyComplete();
                    }
                });
                break;
        }
    }

    @Override
    public boolean isBusy() {
        //可以一直的添加任务
        return false;
    }

    @Override
    public void setBusy(boolean isBusy) {
    }

    private String getCallbackString(boolean b, byte type) {
        String data = Constants.EMPTY_STRING;
        data += Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET;
        data += new String(new byte[]{type});
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
    public String getPersonName(int personID) {
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
        facesNew.clear();
        faceTrack = new YMFaceTrack();
        //此处默认初始化，initCameraMsg()处会根据设备设置自动更改设置
        faceTrack.initTrack(context, orientation, resizeScale);
        faceTrack.setRecognitionConfidence(80);
        this.iw = iw;
        this.ih = ih;
        Doraemon.getInstance(context).addCommand(new SoundCommand("开始添加认识的人", SoundCommand.InputSource.TIPS));
        return true;
    }

    /**
     * 添加人脸数据
     *
     * @param
     * @return
     */
    private boolean addFace(AddFaceCommand command) {
        LogUtils.d(TAG, "addFace");

        if (faceTrack == null)
            return false;
        List<YMFace> face = null;
        if (command.faceType == AddFaceType.YUV)
            face = faceTrack.trackMulti(command.data, iw, ih);
        else if (command.faceType == AddFaceType.IMAGE)
            face = faceTrack.detectMultiBitmap(BitmapFactory.decodeByteArray(command.data, 0, command.data.length));

        if (face != null && face.size() > 0) {
            facesNew.add(new Face(command.faceType, command.data));
            Doraemon.getInstance(context).addCommand(new SoundCommand("添加了一张照片", SoundCommand.InputSource.TIPS));
        }
        return face != null && face.size() > 0;
    }

    /**
     * 设置人脸对应的人名
     *
     * @param name
     * @return
     */
    private boolean setPersonName(String name) {
        LogUtils.d(TAG, "setPersonName:" + name);

        if (facesNew.isEmpty() || faceTrack == null) return false;

        Face face = facesNew.get(0);
        if (face.faceType == AddFaceType.YUV)
            faceTrack.track(face.data, iw, ih);
        else if (face.faceType == AddFaceType.IMAGE) {
            faceTrack.detectMultiBitmap(BitmapFactory.decodeByteArray(face.data, 0, face.data.length));
        }
        int personId = faceTrack.identifyPerson(0);
        if (personId != INCOGNIZANT) {
            //说明已经添加过，则覆盖
            faceTrack.deletePerson(personId);
            persons.remove(String.valueOf(personId));
            App.instance.getDaoSession().getPersonDao().queryBuilder().
                    where(PersonDao.Properties.PersonId.eq(personId)).buildDelete().executeDeleteWithoutDetachingEntities();
        }
        personId = faceTrack.addPerson(0);
        if (personId == INCOGNIZANT)
            return false;

        for (int i = 1; i < facesNew.size(); i++) {
            face = facesNew.get(i);
            if (face.faceType == AddFaceType.YUV)
                faceTrack.track(face.data, iw, ih);
            else if (face.faceType == AddFaceType.IMAGE) {
                faceTrack.detectMultiBitmap(BitmapFactory.decodeByteArray(face.data, 0, face.data.length));
            }
            faceTrack.updatePerson(personId, 0);
        }
        facesNew.clear();
        persons.put(personId, name);
        faceTrack.onRelease();
        faceTrack = null;
        Person entity = new Person();
        entity.setName(name);
        entity.setPersonId(personId);
        App.instance.getDaoSession().getPersonDao().insert(entity);
        Doraemon.getInstance(context).addCommand(new SoundCommand(String.format(context.getString(R.string.format_add_face_success), name), SoundCommand.InputSource.TIPS));
        context.sendBroadcast(new Intent(Constants.ACTION_DORAEMON_REINIT_FACE_TRACK));
        return true;
    }

    private boolean deleteAddPerson(int orientation, int resizeScale) {
        if (faceTrack == null) {
            faceTrack = new YMFaceTrack();
            //此处默认初始化，initCameraMsg()处会根据设备设置自动更改设置
            faceTrack.initTrack(context, orientation, resizeScale);
            faceTrack.setRecognitionConfidence(80);
        }
        boolean result = faceTrack.resetAlbum() > -1;
        faceTrack.onRelease();
        faceTrack = null;
        Doraemon.getInstance(context).addCommand(new SoundCommand("删除了所有认识的人", SoundCommand.InputSource.TIPS));
        context.sendBroadcast(new Intent(Constants.ACTION_DORAEMON_REINIT_FACE_TRACK));
        return result;
    }

    private void notifyComplete() {
        if (activeCommand != null)
            EventBus.getDefault().post(new FaceControlCompleteEvent(activeCommand.getId()));
    }

    class Face {
        public AddFaceType faceType;
        public byte[] data;

        public Face(AddFaceType faceType, byte[] data) {
            this.faceType = faceType;
            this.data = data;
        }
    }
}
