package com.geeknewbee.doraemon.processcenter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.entity.StudyWords;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.iflytek.ise.result.Result;
import com.geeknewbee.doraemon.iflytek.speech.setting.TtsSettings;
import com.geeknewbee.doraemon.iflytek.speech.util.ApkInstaller;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.utils.XmlResultParser;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.BaseResponseBody;
import com.geeknewbee.doraemonsdk.utils.LogUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by GYY on 2016/9/22.
 */
public class LearnEnglish {
    private boolean isLearnning;
    private final static String PREFER_NAME = "ise_settings";
    private final static int REQUEST_CODE_SETTINGS = 1;
    private static String TAG = LearnEnglish.class.getSimpleName();
    private String[] words = new String[]{
            "dog", "apple", "happy", "japan", "tree"
    };
    private int score;
    private int study_index;
    private String auth_token;
    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                LogUtils.d(TAG, "初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };
    // 语记安装助手类
    ApkInstaller mInstaller;
    // private EditText mEvaTextEditText;
    //  private EditText mResultEditText;
    // private Button mIseStartButton;
    // 评测语种
    private String language;
    // 评测题型
    private String category;
    // 结果等级
    private String result_level;
    private String mLastResult;
    // 评测监听接口
    private EvaluatorListener mEvaluatorListener = new EvaluatorListener() {

        @Override
        public void onResult(EvaluatorResult result, boolean isLast) {
            Log.d(TAG, "evaluator result :" + isLast);

            if (isLast) {
                StringBuilder builder = new StringBuilder();
                builder.append(result.getResultString());

                mLastResult = builder.toString();

                LogUtils.d(TAG, "评测结束");
                getEvaluationResult();
            }
        }

        @Override
        public void onError(SpeechError error) {

            Log.d(TAG, "evaluator over");

        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.d(TAG, "evaluator begin");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.d(TAG, "evaluator stoped");
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            LogUtils.e("当前音量：" + volume);
//            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

    };
    private SpeechEvaluator mIse;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "xiaolin";
    private SharedPreferences mSharedPreferences;
    // 引擎类型，在线合成或离线合成
    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            LogUtils.d(TAG, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            LogUtils.d(TAG, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            LogUtils.d(TAG, "继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {

        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {

        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                LogUtils.d(TAG, "播放完成");
            } else {
                LogUtils.d(TAG, error.getPlainDescription(true));
            }
            //TODO
            starEvaluation();
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
    private ApiService apiService;
    private StudyWords studyWords;
    private int oneWordScore;

    private void getStudyWords() {
        if (TextUtils.isEmpty(auth_token)) {
            startSynthesis("令牌不正确，请再试一次。");
            return;
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BuildConfig.URLDOMAIN)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
            apiService.study_words(auth_token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<BaseResponseBody<StudyWords>>() {
                        @Override
                        public void call(BaseResponseBody<StudyWords> studyWordsBaseResponse) {

                            if (studyWordsBaseResponse.isSuccess() && studyWordsBaseResponse.getCode() == 200) {
                                studyWords = studyWordsBaseResponse.getData();
//                                studyWords.getWords().toArray(words);
                                if (studyWords.getWords().size() > 0) {
                                    oneWordScore = 100 / studyWords.getWords().size();
                                    //LogUtils.e("开始学习英语。请跟我一起读。" + studyWords.getWords().get(study_index));
                                    startSynthesis("开始学习英语。请先听一个故事，然后跟我一起读:" + studyWords.getStory() + "。现在跟我一起读：" + studyWords.getWords().get(study_index));
                                } else {
                                    oneWordScore = 0;
                                    LogUtils.d(TAG, "出现错误，暂无单词可学。");
                                    mTts.startSpeaking("出现错误，暂无单词可学。", null);
                                    return;
                                }
                            } else {
                                LogUtils.d(TAG, "出现错误，请再试一次。错误内容：" + studyWordsBaseResponse.getMsg());
                                mTts.startSpeaking("出现错误，请再试一次。错误内容：" + studyWordsBaseResponse.getMsg(), null);
                                return;
                            }
                        }
                    });
        }
    }

    private void getEvaluationResult() {
        // 解析最终结果
        if (!TextUtils.isEmpty(mLastResult)) {
            XmlResultParser resultParser = new XmlResultParser();
            Result result = resultParser.parse(mLastResult);

            if (null != result) {
                int ts = (int) result.total_score;
                score += ts * oneWordScore / 5;
                String eval_text = null;
                switch (ts) {
                    case 0:
                        eval_text = "能不能好好读！";
                        break;
                    case 1:
                        eval_text = "你学的好差！";
                        break;
                    case 2:
                        eval_text = "加油！只有" + (oneWordScore * 2 / 5) + "分。";
                        break;
                    case 3:
                        eval_text = "学得很好。还需要努力。";
                        break;
                    case 4:
                        eval_text = "很标准了。";
                        break;
                    case 5:
                        eval_text = "非常完美。好评！";
                        break;
                }
                if (eval_text != null) {
                    study_index++;
                    if (study_index < studyWords.getWords().size()) {
                        startSynthesis(eval_text + "。继续。" + studyWords.getWords().get(study_index));
                    } else {
                        startSynthesis(eval_text + "。本次学习结束！总分是" + score + "分。");
                    }
                }
            } else {
                LogUtils.d(TAG, "结析结果为空");
            }
        }
    }

    public void init() {
        score = 0;
        study_index = 0;
        auth_token = DoraemonInfoManager.getInstance(App.mContext).getToken();
        isLearnning = true;
        mIse = SpeechEvaluator.createEvaluator(App.mContext, null);
        setEvaText();
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(App.mContext, mTtsInitListener);
        mSharedPreferences = App.mContext.getSharedPreferences(TtsSettings.PREFER_NAME, App.mContext.MODE_PRIVATE);
        mInstaller = new ApkInstaller(App.mContext);
        //startSynthesis("开始学习英语喽。");
        getStudyWords();
    }

    /**
     * 语音合成
     */
    private void startSynthesis(String text) {
        // String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
        // 设置参数
        setParamSynthesis();
        int code = mTts.startSpeaking(text, mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                //未安装则跳转到提示安装页面
                mInstaller.install();
            } else {
                LogUtils.d(TAG, "语音合成失败,错误码: " + code);
            }
        }
        if (study_index >= studyWords.getWords().size()) {
            LogUtils.d(TAG, "等待退出学习英语！总分＝" + score);
            while (mTts.isSpeaking()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            apiService.study_record(auth_token, studyWords.getWid(), score)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<BaseResponseBody>() {
                        @Override
                        public void call(BaseResponseBody baseResponse) {
                            if (!baseResponse.isSuccess()) {
                                LogUtils.d(TAG, "提交学习英语分数失败：" + baseResponse.getMsg());
                            }
                            return;
                        }
                    });
            LogUtils.d(TAG, "退出学习英语！");
            //  通知机器猫可以被唤醒了
            EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
            study_index = 0;
            mTts.destroy();
            mIse.destroy();
            isLearnning = false;
        }
    }

    /**
     * 参数设置
     */
    private void setParamSynthesis() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
//            // 设置在线合成发音人
//            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
//            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        // 设置合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    /**
     * 开始测评
     */
    private void starEvaluation() {
        if (mIse == null) {
            return;
        }
        mLastResult = null;
        setParams();
        mIse.startEvaluating(studyWords.getWords().get(study_index), null, mEvaluatorListener);
    }

    // 设置评测试题
    private void setEvaText() {
        SharedPreferences pref = App.mContext.getSharedPreferences(PREFER_NAME, App.mContext.MODE_PRIVATE);
        language = pref.getString(SpeechConstant.LANGUAGE, "en_us");
        category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence");

        String text = "";
        if ("en_us".equals(language)) {
            if ("read_word".equals(category)) {
                text = App.mContext.getString(R.string.text_en_word);
            } else if ("read_sentence".equals(category)) {
                text = App.mContext.getString(R.string.text_en_sentence);
            }
        } else {
            // 中文评测
            if ("read_syllable".equals(category)) {
                text = App.mContext.getString(R.string.text_cn_syllable);
            } else if ("read_word".equals(category)) {
                text = App.mContext.getString(R.string.text_cn_word);
            } else if ("read_sentence".equals(category)) {
                text = App.mContext.getString(R.string.text_cn_sentence);
            }
        }


        mLastResult = null;

    }

    private void showTip(String str) {
        if (!TextUtils.isEmpty(str)) {
//            mToast.setText(str);
//            mToast.show();
            LogUtils.d(TAG, "Test:" + str);
        }
    }

    private void setParams() {
        SharedPreferences pref = App.mContext.getSharedPreferences(PREFER_NAME, App.mContext.MODE_PRIVATE);
        // 设置评测语言
        language = pref.getString(SpeechConstant.LANGUAGE, "en_us");
        // 设置需要评测的类型
        category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence");
        // 设置结果等级（中文仅支持complete）
        result_level = pref.getString(SpeechConstant.RESULT_LEVEL, "complete");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        String vad_bos = pref.getString(SpeechConstant.VAD_BOS, "1000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        String vad_eos = pref.getString(SpeechConstant.VAD_EOS, "1000");
        // 语音输入超时时间，即用户最多可以连续说多长时间；
        String speech_timeout = pref.getString(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");

        mIse.setParameter(SpeechConstant.LANGUAGE, language);
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, category);
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mIse.setParameter(SpeechConstant.VAD_BOS, vad_bos);
        mIse.setParameter(SpeechConstant.VAD_EOS, vad_eos);
        mIse.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, speech_timeout);
        mIse.setParameter(SpeechConstant.RESULT_LEVEL, result_level);

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIse.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIse.setParameter(SpeechConstant.ISE_AUDIO_PATH, Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/ise.wav");
    }

    public boolean isLearnning() {
        return isLearnning;
    }

    public void stop() {
        if (mTts != null) {
            mTts.stopSpeaking();
        }
    }

    public void destory() {
        if (mIse != null) {
            mIse.cancel();
        }
    }
}
