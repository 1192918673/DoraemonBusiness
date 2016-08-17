package com.geeknewbee.doraemon.processcenter;

import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.utils.SensorUtil;
import com.geeknewbee.doraemon.webservice.BaseResponseBody;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemon.webservice.SoundService;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 把声音string去服务器解析command ，这个是串行的任务队列。
 * 按照先来后到的顺序去执行。
 */
public class SoundTranslateTaskQueue extends AbstractTaskQueue<SoundTranslateInput, List<Command>> {
    private volatile static SoundTranslateTaskQueue instance;
    private OnTranslatorListener translatorListener;

    public static SoundTranslateTaskQueue getInstance() {
        if (instance == null) {
            synchronized (SoundTranslateTaskQueue.class) {
                if (instance == null) {
                    instance = new SoundTranslateTaskQueue();
                }
            }
        }
        return instance;
    }

    public void setTranslatorListener(OnTranslatorListener translatorListener) {
        this.translatorListener = translatorListener;
    }

    @Override
    public List<Command> performTask(SoundTranslateInput input) {
        // 1.先过滤本地命令
        List<Command> localResponse = localPerform(input.input);
        if (localResponse != null) return localResponse;

        // 2.再请求后台，走我们的13万库
        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        SoundService service = retrofit.create(SoundService.class);
        try {
            Response<BaseResponseBody<List<Command>>> response = service.translateSound(input.input).execute();
            if (response.isSuccessful() && response.body().isSuccess()) {
                //TODO 根据现在的getAnswer 接口 形式来 转化成 Command
                return response.body().getData();
            } else
                // 3.如果以上结果都为空，就使用三方(如思必驰)的响应结果
                return Arrays.asList(new Command(CommandType.PLAY_SOUND, input.asrOutput));
        } catch (IOException e) {
            return Arrays.asList(new Command(CommandType.PLAY_SOUND, input.asrOutput));
        }
    }

    /**
     * 本地响应处理
     *
     * @param input
     * @return
     */
    private List<Command> localPerform(String input) {
        if (input.indexOf("你好") != -1) {
            return Arrays.asList(new Command(CommandType.PLAY_SOUND, "你好"));
        }
        if (input.indexOf("自我介绍") != -1) {
            return Arrays.asList(new Command(CommandType.PLAY_SOUND, "《我叫哆啦欸梦》，《出生地是日本东京》，《我的生日是二一一二年九月三日》，《 最喜欢吃》，《铜锣烧》，《害怕老鼠》，《现在通过时光机来到了二十一世纪》"));
        }
        if (input.indexOf("温度") != -1) {
            return Arrays.asList(new Command(CommandType.PLAY_SOUND, "现在室内温度是" + SensorUtil.getInstance().temperture + "度"));
        }
        if (input.indexOf("湿度") != -1) {
            return Arrays.asList(new Command(CommandType.PLAY_SOUND, "现在室内湿度是" + SensorUtil.getInstance().humidity + "度"));
        }
        if (input.indexOf("光强度") != -1) {
            return Arrays.asList(new Command(CommandType.PLAY_SOUND, "现在室内光强度是" + SensorUtil.getInstance().light + "度"));
        }
        if (input.indexOf("向前走") != -1 || input.indexOf("前走") != -1 || input.indexOf("前进") != -1) {

        }
        if (input.indexOf("向后走") != -1 || input.indexOf("后走") != -1 || input.indexOf("后退") != -1) {

        }
        if (input.indexOf("左转") != -1 || input.indexOf("往左转") != -1 || input.indexOf("向左转") != -1 || input.indexOf("向左") != -1) {

        }
        if (input.indexOf("右转") != -1 || input.indexOf("往右转") != -1 || input.indexOf("向右转") != -1 || input.indexOf("向右") != -1) {

        }
        if (input.indexOf("举手") != -1 || input.indexOf("伸胳膊") != -1 || input.indexOf("抬头") != -1) {

        }
        if (input.indexOf("跳小苹果") != -1 || input.indexOf("跳个小苹果") != -1 || input.indexOf("跳个舞") != -1) {

        }
        return null;
    }

    @Override
    public void onTaskComplete(List<Command> output) {
        if (translatorListener != null)
            translatorListener.onTranslateComplete(output);
        // 也就是：此处调的是brain.onTranslateComplete(output);再addCommands命令集合
    }

    public static interface OnTranslatorListener {
        /**
         * 声音翻译完成
         *
         * @param commands
         */
        void onTranslateComplete(List<Command> commands);
    }
}
