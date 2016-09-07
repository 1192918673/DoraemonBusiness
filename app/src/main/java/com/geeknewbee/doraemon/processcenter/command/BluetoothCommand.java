package com.geeknewbee.doraemon.processcenter.command;

import android.text.TextUtils;

import com.geeknewbee.doraemon.R;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.processcenter.LocalResourceManager;

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
     * 肢体运动命令
     */
    private LimbCommand limbCommand;
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
     * 根据蓝牙指令获取对应的Command
     *
     * @return
     */
    public List<Command> getCommand() {
        List<Command> commands = new ArrayList<>();
        if (!TextUtils.isEmpty(faceName)) {
            commands.add(new ExpressionCommand(faceName, 1));
        }

        if (!TextUtils.isEmpty(sound)) {
            commands.add(new SoundCommand(sound, SoundCommand.InputSource.TIPS));
        }

        if (limbCommand != null) {
            commands.addAll(limbCommand.getCommand());
        }

        if (!TextUtils.isEmpty(musicName)) {
            if (musicName.equalsIgnoreCase(Constants.STOP_FLAG))
                commands.add(new Command(CommandType.STOP, musicName));
            else
                commands.add(new Command(CommandType.PLAY_MUSIC, musicName));
        }

        if (wifiInfo != null) {
            commands.add(new WifiCommand(wifiInfo.SSID, wifiInfo.pwd, wifiInfo.type));
        }

        if (!TextUtils.isEmpty(volume)) {
            commands.add(new Command(CommandType.SETTING_VOLUME, volume));
        }

        if (sportActions != null && !sportActions.isEmpty()) {
            commands.add(new ActionSetCommand(sportActions));
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

        if (!TextUtils.isEmpty(action)) {
            //原有的外包手机控制端直接发送的命令字
            switch (action) {
                case "intro_self":
                    commands.add(new SoundCommand(Constants.SELF_INTRODUCTION, SoundCommand.InputSource.SOUND_TRANSLATE));
                    break;
                case "dance":
                    commands.add(new LocalResourceCommand(R.raw.little_apple));
                    commands.add(LocalResourceManager.getInstance().getActionSetCommand(LocalResourceManager.XIAO_PING_GUO));
                    break;
                case "movie":
                    break;
                case "stop":
                    commands.add(new Command(CommandType.STOP));
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

    private static class LimbCommand {
        /**
         * 肢体动作命令
         */
        private List<String> limbData;

        /**
         * 脚步动作
         */
        private FootCommand footCommand;

        public List<Command> getCommand() {
            List<Command> result = new ArrayList<>();
            if (limbData != null && !limbData.isEmpty()) {
                for (String s : limbData) {
                    result.add(new Command(CommandType.MECHANICAL_MOVEMENT, s));
                }
            }

            if (footCommand != null) {
                LeXingCommand command = new LeXingCommand(footCommand.v, footCommand.w, footCommand.duration);
                result.add(command);
            }
            return result;
        }

        private static class FootCommand {
            /**
             * 线速度
             */
            public int v;
            /**
             * 角速度
             */
            public int w;

            /**
             * 持续时间 ms
             */
            public int duration = 0;
        }
    }

    public static class FootCommand {

        public FootCommand(int v, int w) {
            this.v = v;
            this.w = w;
        }

        public FootCommand(int v, int w, int duration) {
            this.v = v;
            this.w = w;
            this.duration = duration;
        }


        public int v;
        public int w;
        /**
         * 持续时间 ms
         */
        public int duration = 0;
    }
}
