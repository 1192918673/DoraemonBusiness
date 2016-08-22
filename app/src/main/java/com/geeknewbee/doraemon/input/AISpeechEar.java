package com.geeknewbee.doraemon.input;

import android.text.TextUtils;

import com.aispeech.AIError;
import com.aispeech.AIResult;
import com.aispeech.IMergeRule;
import com.aispeech.common.AIConstant;
import com.aispeech.common.JSONResultParser;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalGrammarEngine;
import com.aispeech.export.engines.AIMixASREngine;
import com.aispeech.export.listeners.AIASRListener;
import com.aispeech.export.listeners.AILocalGrammarListener;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.processcenter.EventManager;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.GrammarHelper;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.geeknewbee.doraemonsdk.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * 思必驰 实现 ear
 */
public class AISpeechEar implements IEar {

    public static final String TAG = AISpeechEar.class.getSimpleName();
    private AIMixASREngine mASREngine;
    private AILocalGrammarEngine mGrammarEngine;
    private IEar.ASRListener asrListener;
    private boolean needStartRecognitionFlag;//是否需要在初始引擎成功后启动监听,存在调用startRecognition 时候mASREngine==null的情况
    //正在监听标示
    private boolean isListening;

    public AISpeechEar() {
        init();
    }

    private AIMixASREngine init() {
        // 检测是否已生成并存在识别资源，若已存在，则立即初始化本地识别引擎，否则等待编译生成资源文件后加载本地识别引擎
        if (new File(Util.getResourceDir(BaseApplication.mContext) + File.separator + AILocalGrammarEngine.OUTPUT_NAME)
                .exists()) {
            mASREngine = initAsrEngine();// 2.初始化混合识别引擎
            LogUtils.d(TAG, "mASREngine=:" + mASREngine);
        } else {
            initGrammarEngine(); // 1.初始化编译引擎
        }
        return mASREngine;
    }

    /**
     * 1.初始化资源编译引擎
     */
    private void initGrammarEngine() {
        if (mGrammarEngine != null) {
            mGrammarEngine.destroy();
        }
        LogUtils.d(TAG, "grammar create");
        mGrammarEngine = AILocalGrammarEngine.createInstance();// 获取实例
        mGrammarEngine.setResFileName(SpeechConstants.ebnfc_res);// 设置资源文件名
        mGrammarEngine.init(BaseApplication.mContext, new AILocalGrammarListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);// 初始化
        mGrammarEngine.setDeviceId(Util.getIMEI(BaseApplication.mContext));// 设置设备Id

        GrammarHelper gh = new GrammarHelper(BaseApplication.mContext);
//        String contactString = gh.getConatcts();// (1)获取ebnf语法格式的联系人序列字符串
//        String appString = gh.getApps();// (2)获取ebnf语法格式的应用程序名称序列字符串
//        if (TextUtils.isEmpty(contactString)) {
//            contactString = "无联系人";
//        }
        String ebnf = gh.importAssets("", "", "grammar.xbnf");// (3)将获取到的联系人、应用程序名称添加至grammar.xbnf
        LogUtils.d(TAG, ebnf);

        mGrammarEngine.setEbnf(ebnf);// 设置ebnf语法
        mGrammarEngine.update();// 启动语法编译引擎，更新资源
    }

    /**
     * 2.初始化混合识别引擎
     *
     * @return
     */
    private AIMixASREngine initAsrEngine() {
        if (mASREngine != null) {
            return mASREngine;
        }
        LogUtils.d(TAG, "ASR create");
        mASREngine = AIMixASREngine.createInstance();// 获取实例
        LogUtils.d(TAG, "mASREngine=:" + mASREngine);
        mASREngine.setResBin(SpeechConstants.ebnfr_res);// 设置声学资源名
        mASREngine.setNetBin(AILocalGrammarEngine.OUTPUT_NAME, true);// 设置网络资源名
        mASREngine.setVadResource(SpeechConstants.vad_res);// 设置Vad资源名
        /*if (getExternalCacheDir() != null) {
            mASREngine.setTmpDir(getExternalCacheDir().getAbsolutePath());
            mASREngine.setUploadEnable(true);
            mASREngine.setUploadInterval(1000);
        }*/
        mASREngine.setServer("ws://s-test.api.aispeech.com:10000");// 设置服务器地址，默认不用设置   version 1:ws://s.api.aispeech.com:10000
        mASREngine.setRes("robot");// 设置请求的资源名 version 1：robot；version 2：aihome
        mASREngine.setUseXbnfRec(true);// 设置是否启用基于语法的语义识别
        mASREngine.setUsePinyin(true);
        mASREngine.setUseForceout(false);
        mASREngine.setAthThreshold(0.6f);//设置本地置信度阀值
        mASREngine.setIsRelyOnLocalConf(false);//是否开启依据本地置信度优先输出,如需添加例外
        mASREngine.setIsPreferCloud(true);//是否当云端结果有输出时，优先输出云端结果
//        mASREngine.setLocalBetterDomains(new String[]{"aihomeopen", "aihomegoods", "aihomeplay", "aihomenum", "aihomenextup", "aihomehello"});//设置本地擅长的领域范围
        mASREngine.setLocalBetterDomains(new String[]{});//设置本地擅长的领域范围
        mASREngine.setWaitCloudTimeout(2000);// 设置等待云端识别结果超时时长
        mASREngine.setCloudVadEnable(true);// 设置是否启用云端vad,默认开启
        mASREngine.setPauseTime(500);// 设置VAD右边界；VAD普及：静音抑制，或者说它会检测是否有声音
        mASREngine.setUseConf(true);// 设置是否开启置信度
        mASREngine.setAecCfg(SpeechConstants.ace_cfg);
        mASREngine.setConfigName(SpeechConstants.uca_config);
        mASREngine.setUcaParamMode(1);
        mASREngine.setEchoEnable(false);
        mASREngine.setNoSpeechTimeOut(0);// 设置无语音超时时长
        mASREngine.setMaxSpeechTimeS(0);// 设置音频最大录音时长，达到该值将取消语音引擎并抛出异常
        mASREngine.setDeviceId(Util.getIMEI(BaseApplication.mContext));// 设置设备Id
        // 自行设置合并规则:
        // 1. 如果无云端结果,则直接返回本地结果
        // 2. 如果有云端结果,当本地结果置信度大于阈值时,返回本地结果,否则返回云端结果
        mASREngine.setMergeRule(new IMergeRule() {

            @Override
            public AIResult mergeResult(AIResult localResult, AIResult cloudResult) {

                AIResult result = null;
                try {
                    if (cloudResult == null) {
                        // 为结果增加标记,以标示来源于云端还是本地
                        JSONObject localJsonObject = new JSONObject(localResult.getResultObject().toString());
                        localJsonObject.put("src", "native");

                        localResult.setResultObject(localJsonObject);
                        result = localResult;
                    } else {
                        JSONObject cloudJsonObject = new JSONObject(cloudResult.getResultObject().toString());
                        cloudJsonObject.put("src", "cloud");
                        cloudResult.setResultObject(cloudJsonObject);
                        result = cloudResult;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return result;

            }
        });
        mASREngine.init(BaseApplication.mContext, new AIASRListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);// 初始化
        mASREngine.setUseCloud(true);//该方法必须在init之后,是否使用云端识别
        if (NetworkUtil.isWifiConnected(BaseApplication.mContext)) {
            if (mASREngine != null) {
                mASREngine.setNetWorkState("WIFI");// 设置WIFI状态
            }
        }
        return mASREngine;
    }

    @Override
    public synchronized void startRecognition() {
        if (mASREngine != null) {
            if (isListening()) {
                LogUtils.d(TAG, "asr is listing");
                needStartRecognitionFlag = true;
                return;
            }
            setListerStatue(true);
            needStartRecognitionFlag = false;
            mASREngine.start();
            LogUtils.d(TAG, "startRecognition");
        } else {
            needStartRecognitionFlag = true;
            LogUtils.d(TAG, "startRecognition null");
        }
    }

    @Override
    public synchronized void stopRecognition() {
        if (mASREngine != null) {
            setListerStatue(false);
            mASREngine.stopRecording();
            LogUtils.d(TAG, "stopRecording");
        } else
            LogUtils.d(TAG, "stopRecognition null");
    }

    @Override
    public void setASRListener(ASRListener listener) {
        asrListener = listener;
    }

    private JSONObject getJSONObject(JSONObject semantics, String request) {
        try {
            return semantics.getJSONObject(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    private String getJSONString(JSONObject request, String action) {
        try {
            return request.getString(action);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public synchronized boolean isListening() {
        return isListening;
    }

    private synchronized void setListerStatue(boolean isListening) {
        this.isListening = isListening;
    }

    /**
     * 语法编译引擎监听
     */
    public class AILocalGrammarListenerImpl implements AILocalGrammarListener {

        @Override
        public void onInit(int status) {
            if (status == 0) {
                LogUtils.d(TAG, "资源定制引擎初始化成功");
            } else {
                LogUtils.d(TAG, "资源定制引擎初始化失败");
            }
        }

        @Override
        public void onUpdateCompleted(String recordId, String path) {
            LogUtils.d(TAG, "资源生成/更新成功\n path=" + path + "\n 重新加载识别引擎...");
            mASREngine = initAsrEngine();
            LogUtils.d(TAG, "mASREngine=:" + mASREngine);
            if (needStartRecognitionFlag) {
                startRecognition();
            }
        }

        @Override
        public void onError(AIError error) {
            LogUtils.d(TAG, "资源生成发生错误：" + error.getError());
        }
    }

    /**
     * 混合识别引擎监听
     */
    public class AIASRListenerImpl implements AIASRListener {

        @Override
        public void onInit(int status) {
            if (status == 0) {
                LogUtils.d(TAG, "本地识别引擎初始化成功");
            } else {
                LogUtils.d(TAG, "本地识别引擎初始化失败");
            }
        }

        @Override
        public void onReadyForSpeech() {
            LogUtils.d(TAG, "请说话...");
        }

        @Override
        public void onBeginningOfSpeech() {
            LogUtils.d(TAG, "检测到说话");
            EventManager.sendBeginningOfSpeechEvent();
        }

        @Override
        public void onEndOfSpeech() {
            LogUtils.d(TAG, "检测到语音停止，开始识别...");
        }

        @Override
        public void onResults(AIResult results) {
            setListerStatue(false);
            LogUtils.d(TAG, results.getResultObject().toString());

            if (results.isLast()) {
                EventManager.sendBeginningOfDealWithEvent();
                if (results.getResultType() == AIConstant.AIENGINE_MESSAGE_TYPE_JSON) {
                    JSONResultParser parser = new JSONResultParser(results.getResultObject().toString());
                    String outputString = parser.getResult().optString("output", (String) null);
                    String originSoundString = "";
                    JSONObject semantics = parser.getSemantics();
                    if (outputString == null) {
                        //当没有output 的时候 存在两种情况: input 的值 在 "input" 或者 "rec"中
                        originSoundString = parser.getRec();
                        if (originSoundString == null)
                            originSoundString = parser.getInput();
                    } else if (outputString.startsWith("为您搜索")) {
                        originSoundString = parser.getInput();
                        outputString = "";
                    } else
                        originSoundString = parser.getInput();

                    String action = "";
                    String star_name = "";
                    String music_name = "";
                    if (semantics != null) {
                        JSONObject request = getJSONObject(semantics, "request");
                        action = getJSONString(request, "action");
                        if (TextUtils.equals(action, "播放音乐")) {
                            star_name = getJSONString(getJSONObject(request, "param"), "歌手");
                            music_name = getJSONString(getJSONObject(request, "param"), "歌曲");
                        }
                    }

                    asrListener.onASRResult(originSoundString, outputString, action, star_name, music_name);
                } else
                    EventManager.sendStartAsrEvent();
            }
        }

        @Override
        public void onDoa(String s, double v, double v1) {

        }

        @Override
        public void onError(AIError error) {
            setListerStatue(false);
            LogUtils.d(TAG, "识别发生错误:" + error.getErrId());
            initAsrEngine();
        }

        @Override
        public void onRmsChanged(float rmsdB) {
//            LogUtils.d(TAG, "音频、音量发生改变，RmsDB = " + rmsdB);
        }

        /*@Override
        public void onRecorderReleased() {
            *//*测试中发现有时候 onRecorderReleased 偶尔会onResults 完成后一段时间才回调。
            这样在调用startRecognition的时候，发现isListening还是true，造成了无法继续监听的严重bug。
             为了解决这个bug，利用是否开启监听标示needStartRecognitionFlag，每次startRecognition 并且 isListening是true的时候设置为true，
             当onRecorderReleased的时候判断如何needStartRecognitionFlag为true，还需要startRecognition
             *//*

            *//*Log 日志  可遇不可求的bug
            08-18 13:55:31.424 24518-24518/com.geeknewbee.doraemon D/AISpeechEar: 请说话...
08-18 13:55:34.070 24518-24518/com.geeknewbee.doraemon D/AISpeechEar: 检测到说话
08-18 13:55:36.017 24518-24518/com.geeknewbee.doraemon D/AISpeechEar: {"version":"2.7.3","applicationId":"1462760478859598","recordId":"57b54dd33327935fc60005f6","result":{"post":{},"rec":"","wavetime":2080,"delayframe":11,"systime":9189,"vite_vad":1,"rectime":4594,"prutime":0,"version":"0.0.42.2016.2.18.10:26:04","delaytime":24,"sestime":4594,"res":"aifar.0.0.1","vadtime":2080,"eof":1},"luabin":"0.5.2","src":"native"}
08-18 13:55:36.018 24518-24518/com.geeknewbee.doraemon D/AISpeechEar: :null:::
08-18 13:55:36.018 24518-24518/com.geeknewbee.doraemon D/AISpeechEar: translateSound
08-18 13:55:36.019 24518-24518/com.geeknewbee.doraemon D/AISpeechEar: asr is listing
08-18 13:55:36.019 24518-24518/com.geeknewbee.doraemon D/AISpeechEar: onTranslateComplete
08-18 13:55:36.115 24518-24518/com.geeknewbee.doraemon D/AISpeechEar: 检测到录音机停止
            *//*
            setListerStatue(false);
            if (needStartRecognitionFlag)
                startRecognition();
            LogUtils.d(TAG, "检测到录音机停止");
        }*/
    }
}
