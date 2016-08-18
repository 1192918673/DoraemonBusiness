package com.geeknewbee.doraemon.processcenter;

import android.content.Context;

import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.input.AISpeechEar;
import com.geeknewbee.doraemon.input.IEar;
import com.geeknewbee.doraemon.input.IEye;
import com.geeknewbee.doraemon.input.ReadSenseEye;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.List;


/**
 * 哆啦A梦 单利模式
 */
public class Doraemon implements IEar.ASRListener, IEye.AFRListener {
    private volatile static Doraemon instance;
    private final Context context;
    private IEar ear;
    private IEye eye;
    private Brain brain;

    private Doraemon(Context context) {
        this.context = context;
        ear = new AISpeechEar();
        eye = new ReadSenseEye();
        brain = new Brain();
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
     * @param input     语音识别到的输入文本
     * @param asrOutput 三方的响应结果(如思必驰的库给出的响应信息)
     */
    @Override
    public void onASRResult(String input, String asrOutput, String action, String starName, String musicName) {
        /**
         *
         * 否则需要通过后台服务器进行解析
         */
        LogUtils.d(AISpeechEar.TAG, input + ":" + asrOutput + ":" + action + ":" + starName + ":" + musicName);
        brain.translateSound(new SoundTranslateInput(input, asrOutput, action, starName, musicName));
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
    public synchronized void addCommand(Command command) {
        brain.addCommand(command);
    }

    public synchronized void addCommand(List<Command> commands) {
        brain.addCommand(commands);
    }

}
