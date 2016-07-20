package com.geeknewbee.doraemon.control;

import android.content.Context;
import android.text.TextUtils;

import com.geeknewbee.doraemon.control.base.IEar;
import com.geeknewbee.doraemon.control.base.IEye;
import com.geeknewbee.doraemon.control.base.ILimbs;
import com.geeknewbee.doraemon.control.base.IMouth;
import com.geeknewbee.doraemon.util.Constant;
import com.geeknewbee.doraemon.util.LogUtils;


/**
 * 哆啦A梦
 */
public class Doraemon implements IEar.ASRListener, IEye.AFRListener {
    private volatile static Doraemon instance;
    private final Context context;
    private IEar ear;
    private IEye eye;
    private ILimbs limbs;
    private IMouth mouth;
    private Brain brain;

    private Doraemon(Context context) {
        this.context = context;
        ear = new AISpeechEar();
        eye = new ReadSenseEye();
        limbs = new SDLimbs();
        mouth = new AISpeechMouth();
        brain = new Brain(mouth, limbs);

        boolean init = limbs.init();
        LogUtils.d(Constant.TAG_COMMAND, "init limbs:" + init);
    }

    public static Doraemon getInstance(Context context) {
        if (instance == null) {
            synchronized (Doraemon.class) {
                if (instance == null) {
                    instance = new Doraemon(context);
                }
            }
        }
        return instance;
    }

    /**
     * 开始自动声音识别 Automatic Speech Recognition
     */
    public void startASR() {
        ear.setASRListener(this);
        ear.startRecognition();
    }

    /**
     * 停止自动语音识别
     */
    public void stopASR() {
        ear.setASRListener(null);
        ear.stopRecognition();
    }

    /**
     * 开始自动人脸识别 Automatic face Recognition
     */
    public void startAFR() {
        eye.startRecognition();
        eye.setAFRListener(this);
    }

    /**
     * 停止自动人脸识别
     */
    public void stopAFR() {
        eye.setAFRListener(null);
        eye.stopRecognition();
    }


    /**
     * 语音识别结果
     *
     * @param originSoundString
     * @param outputString
     */
    @Override
    public void onASRResult(String originSoundString, String outputString) {
        /**
         * 如果返回有对应的响应直接声音播放(比如思必驰后台直接返回对应的答复)，
         * 否则需要通过后台服务器进行解析
         */
        LogUtils.d("Doraemon识别结果：", originSoundString + ":" + outputString);
        if (!TextUtils.isEmpty(outputString)) {
            brain.addCommand(new Command(CommandType.PLAY_SOUND, outputString));
        } else if (!TextUtils.isEmpty(originSoundString)) {
            brain.translateSound(originSoundString);
        } else {
            brain.addCommand(new Command(CommandType.PLAY_SOUND, "啥也没听清，请再说一遍吧"));
        }
    }

    /**
     * 检测到人脸
     */
    @Override
    public void onDetectFace() {

    }

    /**
     * 添加指令
     *
     * @param command
     */
    public void addCommand(Command command) {
        brain.addCommand(command);
    }
}
