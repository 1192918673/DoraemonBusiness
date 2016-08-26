package com.geeknewbee.doraemon.processcenter;

import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.BuildConfig;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.GetAnswerResponse;
import com.geeknewbee.doraemon.entity.SoundTranslateInput;
import com.geeknewbee.doraemon.entity.event.SwitchMonitorEvent;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.processcenter.command.ActionSetCommand;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.CommandType;
import com.geeknewbee.doraemon.processcenter.command.ExpressionCommand;
import com.geeknewbee.doraemon.processcenter.command.LocalResourceCommand;
import com.geeknewbee.doraemon.processcenter.command.SoundCommand;
import com.geeknewbee.doraemon.utils.SensorUtil;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.BaseResponseBody;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemonsdk.task.AbstractTaskQueue;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
        // 1.当没有解析到声音的时候不做任何输出,重新开启ASR
        if (TextUtils.isEmpty(input.input)) {
            EventManager.sendStartAsrEvent();
            return null;
        }

        // 2.先过滤本地命令
        List<Command> localResponse = localPerform(input);
        if (localResponse != null) return localResponse;

        // 4.再请求后台，走我们的13万库
        Retrofit retrofit = RetrofitUtils.getRetrofit(BuildConfig.URLDOMAIN);
        ApiService service = retrofit.create(ApiService.class);
        try {
            Response<BaseResponseBody<GetAnswerResponse>> response = service.getAnswer(
                    DoraemonInfoManager.getInstance(App.mContext).getToken(), input.input).execute();
            if (response.isSuccessful() && response.body().isSuccess() && !TextUtils.isEmpty(response.body().getData().getAnswer())) {
                return getCommands(response.body().getData());
            }
        } catch (IOException e) {
            LogUtils.d("SoundTranslateTaskQueue", e.getMessage());
        }

        // 5.如果以上都不能寻找到答案的时候。当思必驰有回复用思必驰的结果，思必驰没有则直接重新开启声音监听
        if (TextUtils.isEmpty(input.asrOutput)) {
            EventManager.sendStartAsrEvent();
            return null;
        } else {
            List<Command> commands = new ArrayList<>();
            commands.add(new SoundCommand(input.asrOutput, SoundCommand.InputSource.SOUND_TRANSLATE));
            return commands;
        }
    }

    private List<Command> getCommands(GetAnswerResponse data) {
        //语音回复
        List<Command> commandList = new ArrayList<>();
        if (!TextUtils.isEmpty(data.getAnswer()))
            commandList.add(new SoundCommand(data.getAnswer(), SoundCommand.InputSource.SOUND_TRANSLATE));

        //本地的GIF 图像
        String localGifResource = data.getLocal_resource();
        if (!TextUtils.isEmpty(localGifResource))
            commandList.add(new ExpressionCommand(localGifResource, 1));

        //现在的动作是固定的几个动作，以后改成服务器生成动作脚步，直接执行
        if (data.getAction() != null && data.getAction().size() > 0) {
            ActionSetCommand actionSetCommand = LocalSportActionManager.getInstance().getActionSetCommand(data.getAction());
            if (actionSetCommand != null)
                commandList.add(actionSetCommand);
        }
        return commandList;
    }

    /**
     * 本地响应处理
     *
     * @param soundTranslateInput
     * @return
     */
    private List<Command> localPerform(SoundTranslateInput soundTranslateInput) {
        String input = soundTranslateInput.input;
        if (TextUtils.equals(soundTranslateInput.action, "播放音乐") || TextUtils.equals(soundTranslateInput.musicName, "一首歌")) {
            if (TextUtils.isEmpty(soundTranslateInput.starName) && TextUtils.isEmpty(soundTranslateInput.musicName)) {
                int i = new Random().nextInt(Constants.musics.size());
                soundTranslateInput.starName = Constants.musics.get(i).get("starName");
                soundTranslateInput.musicName = Constants.musics.get(i).get("musicName");
            }
            return Arrays.asList(new Command(CommandType.PLAY_MUSIC, soundTranslateInput.starName + " " + soundTranslateInput.musicName));
        }
        if (input.contains("睡觉") || input.contains("休息") || input.contains("再见") || input.contains("拜拜")) {
            EventBus.getDefault().post(new SwitchMonitorEvent(SoundMonitorType.EDD));
            List<Command> commands = new ArrayList<>();
            commands.add(new SoundCommand("好的，我去休息了，主人一定要记得再来找我奥", SoundCommand.InputSource.TIPS));
            return commands;
        }
        if (input.contains("你好")) {
            List<Command> commands = new ArrayList<>();
            commands.add(new SoundCommand("你好", SoundCommand.InputSource.SOUND_TRANSLATE));
            return commands;
        }
        if (input.contains("自我介绍")) {
            List<Command> commands = new ArrayList<>();
            commands.add(new SoundCommand(Constants.SELF_INTRODUCTION, SoundCommand.InputSource.SOUND_TRANSLATE));
            return commands;
        }
        if (input.contains("笑话") && (input.contains("将") || input.contains("说") || input.contains("讲"))) {
            return Arrays.asList(new SoundCommand("好的", SoundCommand.InputSource.TIPS), new Command(CommandType.PLAY_JOKE));
        }
        if (input.indexOf("背首诗") != -1) {
            List<Command> commands = new ArrayList<>();
            commands.add(new SoundCommand(Constants.TANG_SHI, SoundCommand.InputSource.SOUND_TRANSLATE));
            return commands;
        }
        if (input.contains("温度")) {
            List<Command> commands = new ArrayList<>();
            commands.add(new SoundCommand("现在室内温度是" + SensorUtil.getInstance().temperture + "度", SoundCommand.InputSource.SOUND_TRANSLATE));
            return commands;
        }
        if (input.contains("湿度") || input.contains("适度") || input.contains("十度")) {
            List<Command> commands = new ArrayList<>();
            commands.add(new SoundCommand("现在室内湿度是" + SensorUtil.getInstance().humidity + "度", SoundCommand.InputSource.SOUND_TRANSLATE));
            return commands;
        }
        if (input.contains("光强度")) {
            List<Command> commands = new ArrayList<>();
            commands.add(new SoundCommand("现在室内光强度是" + SensorUtil.getInstance().light + "度", SoundCommand.InputSource.SOUND_TRANSLATE));
            return commands;
        }
        if (input.contains("向前走") || input.contains("前走") || input.contains("前进")) {
            List<Command> commands = new ArrayList<>();
            commands.add(LocalSportActionManager.getInstance().getActionSetCommand("forward"));
            return commands;
        }
        if (input.contains("向后走") || input.contains("后走") || input.contains("后退")) {
            List<Command> commands = new ArrayList<>();
            commands.add(LocalSportActionManager.getInstance().getActionSetCommand("backward"));
            return commands;
        }
        if (input.contains("左转") || input.contains("往左转") || input.contains("向左转") || input.contains("向左")) {
            List<Command> commands = new ArrayList<>();
            commands.add(LocalSportActionManager.getInstance().getActionSetCommand("left"));
            return commands;
        }
        if (input.contains("右转") || input.contains("往右转") || input.contains("向右转") || input.contains("向右")) {
            List<Command> commands = new ArrayList<>();
            commands.add(LocalSportActionManager.getInstance().getActionSetCommand("right"));
            return commands;
        }
        if (input.contains("举手") || input.contains("伸胳膊") || input.contains("抬头")) {
            List<Command> commands = new ArrayList<>();
            commands.add(LocalSportActionManager.getInstance().getActionSetCommand(Arrays.asList("l_arm_up", "r_arm_up")));
            return commands;
        }
        if (input.contains("跳小苹果") || input.contains("跳个小苹果") || input.contains("跳个舞")) {
            List<Command> commands = new ArrayList<>();
            commands.add(new LocalResourceCommand(R.raw.little_apple));
            commands.add(LocalSportActionManager.getInstance().getActionSetCommand(LocalSportActionManager.XIAO_PING_GUO));
            return commands;
        }
        return null;
    }

    @Override
    public void onTaskComplete(List<Command> output) {
        if (translatorListener != null)
            translatorListener.onTranslateComplete(output);
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
