package com.geeknewbee.doraemon.processcenter.command;

import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过蓝牙和控制App传递的指令格式(对外的接口)
 */
public class BluetoothCommand {
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
            commands.add(new Command(CommandType.PLAY_SOUND, sound));
        }

        if (limbCommand != null) {
            commands.addAll(limbCommand.getCommand());
        }

        if (!TextUtils.isEmpty(musicName)) {
            if (musicName.equalsIgnoreCase(Constants.STOP_FLAG))
                commands.add(new Command(CommandType.PLAY_MUSIC.STOP, musicName));
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

            if (footCommand != null)
                result.add(new LeXingCommand(footCommand.v, footCommand.w));
            return result;
        }
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
    }
}
