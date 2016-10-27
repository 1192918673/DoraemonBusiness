package com.geeknewbee.doraemon.processcenter.command;

import android.text.TextUtils;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.event.SwitchControlTypeEvent;
import com.geeknewbee.doraemon.processcenter.ControlType;
import com.geeknewbee.doraemon.processcenter.LocalResourceManager;
import com.geeknewbee.doraemon.processcenter.SportActionUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通过蓝牙和控制App传递的指令格式(对外的接口)
 */
public class BluetoothCommand {
    /**
     * 舞蹈
     */
    public String danceName;
    /**
     * 舞蹈对应音乐资源名字
     */
    public String danceMusicName;
    /**
     * action:原来版本APP发送的命令
     */
    public String action;
    /**
     * 表情名字
     */
    private String faceName;
    /**
     * 声音
     */
    private String sound;

    /**
     * 歌曲名称
     */
    private String musicName;
    /**
     * WIFI信息
     */
    private WifiInfo wifiInfo;
    /**
     * Volume值
     */
    private String volume;
    /**
     * 跳舞动作
     */
    private List<SportAction> sportActions;

    /**
     * 手动控制脚步
     */
    private FootCommand bluetoothFootCommand;

    /**
     * 声音(IOS_BUSINESS 类型 Sound ios 商业端，需要给它回复TTS完成)
     */
    private String tts;

    /**
     * 动作脚本
     */
    private List<String> lines;

    /**
     * tts ,动作是否覆盖正在进行的  1/0
     */
    private int isOverwrite;

    /**
     * 表情的循环次数
     */
    private int loop;

    /**
     * 根据蓝牙指令获取对应的Command
     *
     * @return
     */
    public List<Command> getCommand() {
        List<Command> commands = new ArrayList<>();
        if (!TextUtils.isEmpty(faceName)) {
            commands.add(new ExpressionCommand(faceName, loop <= 0 ? 1 : loop));
        }

        if (!TextUtils.isEmpty(sound)) {
            commands.add(new SoundCommand(sound, SoundCommand.InputSource.TIPS, isOverwrite == 1));
        }

        if (!TextUtils.isEmpty(musicName)) {
            if (musicName.equalsIgnoreCase(Constants.STOP_FLAG))
                commands.add(new Command(CommandType.STOP, musicName));
            else {
                commands.add(new SoundCommand(App.mContext.getString(R.string.tips_search_music), SoundCommand.InputSource.TIPS));
                commands.add(new Command(CommandType.PLAY_MUSIC, musicName));
            }
        }

        if (wifiInfo != null) {
            commands.add(new SoundCommand(Constants.TIPS_SET_WIFI, SoundCommand.InputSource.TIPS));
            commands.add(new WifiCommand(wifiInfo.SSID, wifiInfo.pwd, wifiInfo.type));
        }

        if (!TextUtils.isEmpty(volume)) {
            commands.add(new Command(CommandType.SETTING_VOLUME, volume));
        }

        if (sportActions != null && !sportActions.isEmpty()) {
            commands.add(new SportActionSetCommand(sportActions, isOverwrite == 1,
                    SportActionSetCommand.InputSource.REMOTE_CONTROL));
        }

        if (!TextUtils.isEmpty(danceName)) {
            if (LocalResourceManager.getInstance().containsAction(danceName)) {
                commands.add(new LocalResourceCommand(danceMusicName));
                commands.add(LocalResourceManager.getInstance().getActionSetCommand(danceName));
            }
        }

        if (bluetoothFootCommand != null) {
            commands.add(new BluetoothControlFootCommand(bluetoothFootCommand.v, bluetoothFootCommand.w));
        }

        if (!TextUtils.isEmpty(tts)) {
            commands.add(new SoundCommand(tts, SoundCommand.InputSource.IOS_BUSINESS, isOverwrite == 1));
        }

        if (lines != null && !lines.isEmpty()) {
            List<SportAction> sportActions = new ArrayList<>();
            for (String line : lines) {
                SportAction sportAction = SportActionUtil.parseSportCommand(line);
                if (sportAction != null)
                    sportActions.add(sportAction);
            }
            commands.add(new SportActionSetCommand(sportActions, isOverwrite == 1,
                    SportActionSetCommand.InputSource.REMOTE_CONTROL));
        }

        if (!TextUtils.isEmpty(action)) {
            //原有的外包手机控制端直接发送的命令字
            switch (action) {
                case "intro_self":
                    commands.add(new SoundCommand(Constants.SELF_INTRODUCTION, SoundCommand.InputSource.TIPS));
                    commands.add(new ExpressionCommand("se", 5));
                    break;
                case "dance":
                    commands.add(new LocalResourceCommand(R.raw.little_apple));
                    commands.add(LocalResourceManager.getInstance().getDanceCommand(LocalResourceManager.XIAO_PING_GUO));
                    break;
                case "movie":
                    break;
                case "stop":
                    commands.add(new Command(CommandType.STOP));
                    break;
                case "say_hi":
                    commands.add(new SoundCommand(Constants.HELLO, SoundCommand.InputSource.TIPS));
                    commands.add(new ExpressionCommand("wei_xiao", 3));
                    commands.add(LocalResourceManager.getInstance().getActionSetCommand(LocalResourceManager.ACTION_ARM_UP_DOWN_MOVE));
                    break;
                case "end_say":
                    commands.add(new SoundCommand(Constants.END, SoundCommand.InputSource.TIPS));
                    commands.add(new ExpressionCommand("wei_xiao", 3));
                    commands.add(LocalResourceManager.getInstance().getActionSetCommand(LocalResourceManager.ACTION_ARM_UP_DOWN_MOVE));
                    break;
                case "sleep":
                    //进入休眠
                    commands.add(new Command(CommandType.SLEEP));
                    break;
                case "open_auto_demonstration":
                    //进入自动展示模式
                    EventBus.getDefault().post(new SwitchControlTypeEvent(ControlType.AUTO));
                    break;
                case "close_auto_demonstration":
                    //退出自动展示模式
                    EventBus.getDefault().post(new SwitchControlTypeEvent(ControlType.LOCAL));
                    break;
                default:
                    commands.add(LocalResourceManager.getInstance().getActionSetCommand(Arrays.asList(action)));
                    break;
            }
        }

        return commands;
    }

    private static class WifiInfo {
        public int type;
        public String SSID;
        public String pwd;
    }

    public static class FootCommand {

        public int v;
        public int w;
        /**
         * 持续时间 ms
         */
        public int duration = 0;

        public FootCommand(int v, int w) {
            this.v = v;
            this.w = w;
        }

        public FootCommand(int v, int w, int duration) {
            this.v = v;
            this.w = w;
            this.duration = duration;
        }
    }
}
