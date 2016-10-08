package com.geeknewbee.doraemon.processcenter.command;

import android.text.TextUtils;

import com.geeknewbee.doraemon.processcenter.SportActionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * IOS BLE蓝牙获取命令
 */
public class BluetoothIOSCommand {

    /**
     * 声音
     */
    private String tts;

    /**
     * 跳舞动作
     */
    private List<String> lines;

    public List<Command> getCommand() {
        List<Command> commands = new ArrayList<>();

        if (!TextUtils.isEmpty(tts)) {
            commands.add(new SoundCommand(tts, SoundCommand.InputSource.IOS_BUSINESS));
        }

        if (lines != null && !lines.isEmpty()) {
            List<SportAction> sportActions = new ArrayList<>();
            for (String line : lines) {
                sportActions.add(SportActionUtil.parseSportCommand(line));
            }
            commands.add(new SportActionSetCommand(sportActions));
        }
        return commands;
    }
}
