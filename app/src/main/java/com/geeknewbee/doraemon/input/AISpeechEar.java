package com.geeknewbee.doraemon.input;

import android.text.TextUtils;
import android.util.Log;

import com.aispeech.AIError;
import com.aispeech.AIResult;
import com.aispeech.common.JSONResultParser;
import com.aispeech.common.Util;
import com.aispeech.export.engines.AILocalGrammarEngine;
import com.aispeech.export.engines.AIMixASREngine;
import com.aispeech.export.listeners.AIASRListener;
import com.aispeech.export.listeners.AILocalGrammarListener;
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.entity.event.ASRResultEvent;
import com.geeknewbee.doraemon.entity.event.ReadyForSpeechEvent;
import com.geeknewbee.doraemon.processcenter.EventManager;
import com.geeknewbee.doraemonsdk.BaseApplication;
import com.geeknewbee.doraemonsdk.utils.GrammarHelper;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
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
    private double mPhis = 0;

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
//        mGrammarEngine = AILocalGrammarEngine.createInstance();// 获取实例
//        mGrammarEngine.setResFileName(SpeechConstants.ebnfc_res);// 设置资源文件名
//        mGrammarEngine.init(BaseApplication.mContext, new AILocalGrammarListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);// 初始化
//        mGrammarEngine.setDeviceId(Util.getIMEI(BaseApplication.mContext));// 设置设备Id
//
//        GrammarHelper gh = new GrammarHelper(BaseApplication.mContext);
////        String contactString = gh.getConatcts();// (1)获取ebnf语法格式的联系人序列字符串
////        String appString = gh.getApps();// (2)获取ebnf语法格式的应用程序名称序列字符串
////        if (TextUtils.isEmpty(contactString)) {
////            contactString = "无联系人";
////        }
//        String ebnf = gh.importAssets("", "", "grammar.xbnf");// (3)将获取到的联系人、应用程序名称添加至grammar.xbnf
//        LogUtils.d(TAG, ebnf);
//
//        mGrammarEngine.setEbnf(ebnf);// 设置ebnf语法
//        mGrammarEngine.update();// 启动语法编译引擎，更新资源

        mGrammarEngine = AILocalGrammarEngine.createInstance();
        mGrammarEngine.setResFileName(SpeechConstants.ebnfc_res);
        mGrammarEngine
                .init(App.mContext, new AILocalGrammarListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        mGrammarEngine.setDeviceId(Util.getIMEI(App.mContext));

        GrammarHelper gh = new GrammarHelper(App.mContext);
        String ebnf = gh.importAssets("", "", "asr.xbnf");
        Log.i(TAG, ebnf);
        // 设置ebnf语法
        mGrammarEngine.setEbnf(ebnf);
        // 启动语法编译引擎，更新资源
        mGrammarEngine.update();
    }

    /**
     * 2.初始化混合识别引擎
     *
     * @return
     */
    private AIMixASREngine initAsrEngine() {
        if (mASREngine != null) {
            mASREngine.destroy();
        }
        LogUtils.d(TAG, "ASR create");
//        mASREngine = AIMixASREngine.createInstance();// 获取实例
//        LogUtils.d(TAG, "mASREngine=:" + mASREngine);
//        mASREngine.setResBin(SpeechConstants.ebnfr_res);// 设置声学资源名
//        mASREngine.setNetBin(AILocalGrammarEngine.OUTPUT_NAME, true);// 设置网络资源名
//        mASREngine.setVadResource(SpeechConstants.vad_res);// 设置Vad资源名
//        /*if (getExternalCacheDir() != null) {
//            mASREngine.setTmpDir(getExternalCacheDir().getAbsolutePath());
//            mASREngine.setUploadEnable(true);
//            mASREngine.setUploadInterval(1000);
//        }*/
//        mASREngine.setServer("ws://s-test.api.aispeech.com:10000");// 设置服务器地址，默认不用设置   version 1:ws://s.api.aispeech.com:10000
//        mASREngine.setRes("robot");// 设置请求的资源名 version 1：robot；version 2：aihome
//        mASREngine.setUseXbnfRec(true);// 设置是否启用基于语法的语义识别
//        mASREngine.setUsePinyin(false);
//        mASREngine.setUseForceout(false);
//        //mASREngine.setEchoWavePath(Environment.getExternalStorageDirectory().getPath());//设置回消音频文件存放路径
//        //mASREngine.setEchoEnable(true);//edd△△△开头的是原始数据；doaLn△△△开头的是aec后的数据
//        mASREngine.setAthThreshold(1.0f);//设置本地置信度阀值
//        mASREngine.setIsRelyOnLocalConf(false);//是否开启依据本地置信度优先输出,如需添加例外
//        mASREngine.setIsPreferCloud(true);//是否当云端结果有输出时，优先输出云端结果
////        mASREngine.setLocalBetterDomains(new String[]{"aihomeopen", "aihomegoods", "aihomeplay", "aihomenum", "aihomenextup", "aihomehello"});//设置本地擅长的领域范围
//        mASREngine.setLocalBetterDomains(new String[]{});//设置本地擅长的领域范围
//        mASREngine.setWaitCloudTimeout(2000);// 设置等待云端识别结果超时时长
//        mASREngine.setCloudVadEnable(true);// 设置是否启用云端vad,默认开启
//        mASREngine.setPauseTime(300);// 设置VAD右边界；VAD普及：静音抑制，或者说它会检测是否有声音
//        mASREngine.setUseConf(true);// 设置是否开启置信度
//        mASREngine.setAecCfg(SpeechConstants.ace_cfg);
//        mASREngine.setConfigName(SpeechConstants.uca_config);
//        mASREngine.setUcaParamMode(1);
//        mASREngine.setNoSpeechTimeOut(0);// 设置无语音超时时长
//        mASREngine.setMaxSpeechTimeS(0);// 设置音频最大录音时长，达到该值将取消语音引擎并抛出异常
//        mASREngine.setDeviceId(Util.getIMEI(BaseApplication.mContext));// 设置设备Id
//        // 自行设置合并规则:
//        // 1. 如果无云端结果,则直接返回本地结果
//        // 2. 如果有云端结果,当本地结果置信度大于阈值时,返回本地结果,否则返回云端结果
//        mASREngine.setMergeRule(new IMergeRule() {
//
//            @Override
//            public AIResult mergeResult(AIResult localResult, AIResult cloudResult) {
//
//                AIResult result = null;
//                try {
//                    if (cloudResult == null) {
//                        // 为结果增加标记,以标示来源于云端还是本地
//                        JSONObject localJsonObject = new JSONObject(localResult.getResultObject().toString());
//                        localJsonObject.put("src", "native");
//
//                        localResult.setResultObject(localJsonObject);
//                        result = localResult;
//                    } else {
//                        JSONObject cloudJsonObject = new JSONObject(cloudResult.getResultObject().toString());
//                        cloudJsonObject.put("src", "cloud");
//                        cloudResult.setResultObject(cloudJsonObject);
//                        result = cloudResult;
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return result;
//
//            }
//        });
//        mASREngine.init(BaseApplication.mContext, new AIASRListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);// 初始化
//        mASREngine.setUseCloud(true);//该方法必须在init之后,是否使用云端识别
//        if (NetworkUtil.isWifiConnected(BaseApplication.mContext)) {
//            if (mASREngine != null) {
//                mASREngine.setNetWorkState("WIFI");// 设置WIFI状态
//            }
//        }

        mASREngine = AIMixASREngine.createInstance();
        mASREngine.setResBin(SpeechConstants.ebnfr_res);
        mASREngine.setNetBin(AILocalGrammarEngine.OUTPUT_NAME, true);

        mASREngine.setVadResource(SpeechConstants.vad_res);
        mASREngine.setServer("ws://s.api.aispeech.com:1028,ws://s.api.aispeech.com:80");
        mASREngine.setRes("aihome");
        mASREngine.setUseXbnfRec(true);
        mASREngine.setUsePinyin(true);
        mASREngine.setUseForceout(false);
        mASREngine.setAthThreshold(0.6f);
        mASREngine.setIsRelyOnLocalConf(true);
        mASREngine.setIsPreferCloud(true);
        mASREngine.setWaitCloudTimeout(5000);
        mASREngine.setPauseTime(500);
        mASREngine.setUseConf(true);
        mASREngine.setNoSpeechTimeOut(15000);//设置无语音超时时长，单位毫秒，默认值为5000ms ；如果达到该设置值时，自动停止录音并放弃请求内核
        mASREngine.setMaxSpeechTimeS(20);// 设置音频最大录音时长，达到该值将取消语音引擎并抛出异常`
        mASREngine.setDeviceId(Util.getIMEI(App.mContext));
        mASREngine.setCloudVadEnable(false);
        mASREngine.setAecCfg(SpeechConstants.ace_cfg);
        mASREngine.setConfigName(SpeechConstants.uca_config); //环形麦的配置
//        mAsrEngine.setConfigName(SampleConstants.ula_config);//线性麦的配置
        mASREngine.setUcaParamMode(2);
        mASREngine.setEchoEnable(false);
        mASREngine.setCloudVadEnable(true);
        mASREngine.init(App.mContext, new AIASRListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        mASREngine.setUseCloud(true);//该方法必须在init之后
        return mASREngine;
    }

    /**
     * 测试发现不能重复的打开ASR
     *
     * @param phis
     */
    @Override
    public void startRecognition(double phis) {
        this.mPhis = phis;
        if (mASREngine != null && !isListening()) {
            needStartRecognitionFlag = false;
            mASREngine.setUcaPhis(phis);
            mASREngine.start();
            LogUtils.d(TAG, "startRecognition");
        } else if (!isListening()) {
            needStartRecognitionFlag = true;
            LogUtils.d(TAG, "startRecognition");
        } else
            LogUtils.d(TAG, "startRecognition");

        setListerStatue(true);
    }

    @Override
    public void stopRecognition() {
        if (mASREngine != null) {
            mASREngine.cancel();
            mASREngine.stopRecording();
            LogUtils.d(TAG, "stopRecognition");
        } else
            LogUtils.d(TAG, "stopRecognition");

        setListerStatue(false);
    }

    @Override
    public void setASRListener(ASRListener listener) {
        asrListener = listener;
    }

    @Override
    public void destroy() {
        if (mASREngine != null) {
            mASREngine.destroy();
            mASREngine = null;
        }
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
    public boolean reInit() {
        init();
        return true;
    }

    @Override
    public boolean isListening() {
        return isListening;
    }

    private void setListerStatue(boolean isListening) {
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
                startRecognition(mPhis);
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
            EventBus.getDefault().post(new ReadyForSpeechEvent());
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
            EventBus.getDefault().post(new ASRResultEvent());
            LogUtils.d(TAG, results.getResultObject().toString());

            EventManager.sendBeginningOfDealWithEvent();
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
                if (TextUtils.equals(action, "播放音乐") || TextUtils.equals(action, "音乐")) {
                    star_name = getJSONString(getJSONObject(request, "param"), "歌手");
                    music_name = getJSONString(getJSONObject(request, "param"), "歌曲");
                }
            }

            if (asrListener != null)
                asrListener.onASRResult(originSoundString, outputString, action, star_name, music_name);
        }

        @Override
        public void onDoa(String s, double v, double v1) {

        }

        @Override
        public void onError(AIError error) {
            setListerStatue(false);
            LogUtils.d(TAG, "识别发生错误:" + error.getErrId());
            mASREngine.cancel();
            mASREngine.stopRecording();
            EventManager.sendStartAsrEvent();
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // LogUtils.d(TAG, "音频、音量发生改变，RmsDB = " + rmsdB);
        }
    }
}
