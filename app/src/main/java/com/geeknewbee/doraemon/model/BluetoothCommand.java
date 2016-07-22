package com.geeknewbee.doraemon.model;

import android.text.TextUtils;

import com.geeknewbee.doraemon.control.Command;
import com.geeknewbee.doraemon.control.CommandType;
import com.geeknewbee.doraemon.util.Constant;
import com.geeknewbee.doraemon.utils.FaceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 通过蓝牙传递的指令
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
     * 根据蓝牙指令获取对应的Command
     *
     * @return
     */
    public List<Command> getCommand() {
        List<Command> commands = new ArrayList<>();
        if (!TextUtils.isEmpty(faceName)) {
            commands.add(new Command(CommandType.SHOW_EXPRESSION, FaceUtil.getResourcesString(faceName)));
        }

        if (!TextUtils.isEmpty(sound)) {
            if (sound.equalsIgnoreCase(Constant.STOP_FLAG))
                commands.add(new Command(CommandType.STOP, sound));
            else
                commands.add(new Command(CommandType.PLAY_SOUND, sound));
        }

        if (limbCommand != null) {
            commands.addAll(limbCommand.getCommand());
        }

        if (!TextUtils.isEmpty(musicName)) {
            commands.add(new Command(CommandType.PLAY_MUSIC, musicName));
        }

        return commands;
    }

    private static class LimbCommand {
        /**
         * 舵机命令
         */
        private List<String> duoJi;
        /**
         * 电机命令
         */
        private List<String> dianJi;

        public List<Command> getCommand() {
            List<Command> result = new ArrayList<>();
            if (duoJi != null && !duoJi.isEmpty()) {
                for (String s : duoJi) {
                    result.add(new Command(CommandType.MECHANICAL_MOVEMENT, s));
                }
            }

            if (dianJi != null && !dianJi.isEmpty()) {
                for (String s : dianJi) {
                    result.add(new Command(CommandType.MECHANICAL_MOVEMENT, s));
                }
            }

            return result;
        }
    }
}
