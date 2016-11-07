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
import com.geeknewbee.doraemon.entity.event.BeginningOfSpeechEvent;
import com.geeknewbee.doraemon.entity.event.BeginningofDealWithEvent;
import com.geeknewbee.doraemon.entity.event.ReadyForSpeechEvent;
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

        mASREngine = AIMixASREngine.createInstance();
        mASREngine.setResBin(SpeechConstants.ebnfr_res);
        mASREngine.setNetBin(AILocalGrammarEngine.OUTPUT_NAME, true);

        mASREngine.setVadResource(SpeechConstants.vad_res);
        mASREngine.setServer("ws://s-test.api.aispeech.com:10000");
        mASREngine.setRes("aihome");
        mASREngine.setUseXbnfRec(true);
        mASREngine.setUsePinyin(true);
        mASREngine.setUseForceout(false);
        mASREngine.setAthThreshold(0.6f);
        mASREngine.setIsRelyOnLocalConf(true);
        mASREngine.setIsPreferCloud(true);
        mASREngine.setWaitCloudTimeout(6000);
        mASREngine.setPauseTime(500);
        mASREngine.setUseConf(true);
        mASREngine.setNoSpeechTimeOut(0);
        mASREngine.setMaxSpeechTimeS(10);
        mASREngine.setDeviceId(Util.getIMEI(App.instance));
        mASREngine.setCloudVadEnable(false);
        mASREngine.setAecCfg(SpeechConstants.ace_cfg);
        mASREngine.setConfigName(SpeechConstants.uca_config);//环形麦的配置
//        mASREngine.setConfigName(SampleConstants.ula_config);//线性麦的配置
        mASREngine.setUcaParamMode(1);
        mASREngine.setEchoEnable(false);
        mASREngine.setCloudVadEnable(true);
        mASREngine.init(App.instance, new AIASRListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);
        mASREngine.setUseCloud(true);//该方法必须在init之后
        mASREngine.setCoreType("cn.sds");
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
            LogUtils.d(TAG, "ASREngine start");
        } else if (mASREngine == null) {
            needStartRecognitionFlag = true;
            LogUtils.d(TAG, "ASREngine start is null");
        } else
            LogUtils.d(TAG, "ASREngine had started");

        setListerStatue(true);
    }

    @Override
    public void stopRecognition() {
        if (mASREngine != null) {
            mASREngine.cancel();
            mASREngine.stopRecording();
            LogUtils.d(TAG, "ASREngine stop");
        } else
            LogUtils.d(TAG, "ASREngine stop is null");

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
            EventBus.getDefault().post(new BeginningOfSpeechEvent());
        }

        @Override
        public void onEndOfSpeech() {
            LogUtils.d(TAG, "检测到语音停止，开始识别...");
            EventBus.getDefault().post(new BeginningofDealWithEvent());
        }

        @Override
        public void onResults(AIResult results) {
            if (!isListening)
                return;
            
            setListerStatue(false);
            LogUtils.d(TAG, results.getResultObject().toString());

            JSONResultParser parser = new JSONResultParser(results.getResultObject().toString());
            String outputString = parser.getResult().optString("output", (String) null);
            String sds = parser.getResult().optString("sds", "");
            try {
                outputString = outputString == null ? getJSONString(new JSONObject(sds), "output") : null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                    star_name = getJSONString(getJSONObject(request, "param"), "歌手名");
                    music_name = getJSONString(getJSONObject(request, "param"), "歌曲名");
                }
            }

            EventBus.getDefault().post(new ASRResultEvent(true, originSoundString, outputString, action, star_name, music_name));
        }

        @Override
        public void onDoa(String s, double v, double v1) {

        }

        @Override
        public void onError(AIError error) {
            if (isListening)
                EventBus.getDefault().post(new ASRResultEvent(false, "", "", "", "", ""));
            setListerStatue(false);
            LogUtils.d(TAG, "识别发生错误:" + error.getErrId());
            mASREngine.cancel();
            mASREngine.stopRecording();
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // LogUtils.d(TAG, "音频、音量发生改变，RmsDB = " + rmsdB);
        }
    }
}
