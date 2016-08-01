package com.geeknewbee.doraemon.model;

import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.control.Command;
import com.geeknewbee.doraemon.control.CommandType;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过蓝牙传递的指令格式
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
    private String wiFiMessage;

    /**
     * Volume值
     */
    private String volume;

    /**
     * 根据蓝牙指令获取对应的Command
     *
     * @return
     */
    public List<Command> getCommand() {
        List<Command> commands = new ArrayList<>();
        if (!TextUtils.isEmpty(faceName)) {
            commands.add(new Command(CommandType.SHOW_EXPRESSION, faceName));
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

        if (!TextUtils.isEmpty(wiFiMessage)) {
            commands.add(new Command(CommandType.WIFI_MESSAGE, wiFiMessage));
        }

        if (!TextUtils.isEmpty(volume)) {
            commands.add(new Command(CommandType.SETTING_VOLUME, volume));
        }

        return commands;
    }

    private static class LimbCommand {
        /**
         * 肢体动作命令
         */
        private List<String> limbData;

        public List<Command> getCommand() {
            List<Command> result = new ArrayList<>();
            if (limbData != null && !limbData.isEmpty()) {
                for (String s : limbData) {
                    result.add(new Command(CommandType.MECHANICAL_MOVEMENT, s));
                }
            }

            return result;
        }
    }
}
