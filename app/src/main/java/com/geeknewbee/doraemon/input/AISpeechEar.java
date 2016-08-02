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
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.SpeechConstants;
import com.geeknewbee.doraemon.control.base.IEar;
import com.geeknewbee.doraemon.utils.GrammarHelper;
import com.geeknewbee.doraemon.utils.LogUtils;
import com.geeknewbee.doraemon.utils.NetworkUtil;

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

    public AISpeechEar() {
        init();
    }

    private AIMixASREngine init() {
        // 检测是否已生成并存在识别资源，若已存在，则立即初始化本地识别引擎，否则等待编译生成资源文件后加载本地识别引擎
        if (new File(Util.getResourceDir(App.mContext) + File.separator + AILocalGrammarEngine.OUTPUT_NAME)
                .exists()) {
            mASREngine = initAsrEngine();// 2.初始化混合识别引擎
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
        mGrammarEngine.init(App.mContext, new AILocalGrammarListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);// 初始化
        mGrammarEngine.setDeviceId(Util.getIMEI(App.mContext));// 设置设备Id

        GrammarHelper gh = new GrammarHelper(App.mContext);
        String contactString = gh.getConatcts();// (1)获取ebnf语法格式的联系人序列字符串
        String appString = gh.getApps();// (2)获取ebnf语法格式的应用程序名称序列字符串
        if (TextUtils.isEmpty(contactString)) {
            contactString = "无联系人";
        }
        String ebnf = gh.importAssets(contactString, appString, "grammar.xbnf");// (3)将获取到的联系人、应用程序名称添加至grammar.xbnf
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
        mASREngine.setResBin(SpeechConstants.ebnfr_res);// 设置声学资源名
        mASREngine.setNetBin(AILocalGrammarEngine.OUTPUT_NAME, true);// 设置网络资源名
        mASREngine.setVadResource(SpeechConstants.vad_res);// 设置Vad资源名
        /*if (getExternalCacheDir() != null) {
            mASREngine.setTmpDir(getExternalCacheDir().getAbsolutePath());
            mASREngine.setUploadEnable(true);
            mASREngine.setUploadInterval(1000);
        }*/
        mASREngine.setServer("ws://s.api.aispeech.com");// 设置服务器地址，默认不用设置
        mASREngine.setRes("robot");// 设置请求的资源名
        mASREngine.setUseXbnfRec(true);// 设置是否启用基于语法的语义识别
        mASREngine.setUsePinyin(true);
        mASREngine.setUseForceout(false);
        mASREngine.setAthThreshold(0.6f);//设置本地置信度阀值
        mASREngine.setIsRelyOnLocalConf(true);//是否开启依据本地置信度优先输出,如需添加例外
        mASREngine.setLocalBetterDomains(new String[]{"aihomeopen", "aihomegoods", "aihomeplay", "aihomenum", "aihomenextup", "aihomehello"});//设置本地擅长的领域范围
        mASREngine.setWaitCloudTimeout(2000);// 设置等待云端识别结果超时时长
        mASREngine.setPauseTime(1000);// 设置VAD右边界
        mASREngine.setUseConf(true);// 设置是否开启置信度
        mASREngine.setNoSpeechTimeOut(0);// 设置无语音超时时长
        mASREngine.setDeviceId(Util.getIMEI(App.mContext));// 设置设备Id
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
        mASREngine.init(App.mContext, new AIASRListenerImpl(), SpeechConstants.APPKEY, SpeechConstants.SECRETKEY);// 初始化
        mASREngine.setUseCloud(true);//该方法必须在init之后,是否使用云端识别
        if (NetworkUtil.isWifiConnected(App.mContext)) {
            if (mASREngine != null) {
                mASREngine.setNetWorkState("WIFI");// 设置WIFI状态
            }
        }
        return mASREngine;
    }

    @Override
    public void startRecognition() {
        if (mASREngine != null) {
            mASREngine.start();
        }
    }

    @Override
    public void stopRecognition() {
        if (mASREngine != null) {
            mASREngine.stopRecording();
        }
    }

    @Override
    public void setASRListener(ASRListener listener) {
        asrListener = listener;
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

        }

        @Override
        public void onEndOfSpeech() {
            LogUtils.d(TAG, "检测到语音停止，开始识别...");
        }

        @Override
        public void onResults(AIResult results) {
            LogUtils.d(TAG, results.getResultObject().toString());

            if (results.isLast()) {
                if (results.getResultType() == AIConstant.AIENGINE_MESSAGE_TYPE_JSON) {
                    JSONResultParser parser = new JSONResultParser(results.getResultObject().toString());
                    String outputString = parser.getResult().optString("output", (String) null);
                    String originSoundString = "0";
                    if (outputString == null) {
                        originSoundString = parser.getRec();
                    } else if (outputString.startsWith("为您搜索")) {
                        originSoundString = parser.getInput();
                    }
                    if (asrListener != null) {
                        asrListener.onASRResult(originSoundString, outputString);
                    }
                }
            }
        }

        @Override
        public void onError(AIError error) {
            LogUtils.d(TAG, "识别发生错误:" + error.getErrId());
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            LogUtils.d(TAG, "音频、音量发生改变，RmsDB = " + rmsdB);
        }

        @Override
        public void onRecorderReleased() {
            LogUtils.d(TAG, "检测到录音机停止");
        }
    }
}
