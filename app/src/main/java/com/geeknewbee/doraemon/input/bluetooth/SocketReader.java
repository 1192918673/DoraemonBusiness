package com.geeknewbee.doraemon.input.bluetooth;

import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocketReader {
    private String data;

    public SocketReader() {
        data = Constants.EMPTY_STRING;
    }

    public synchronized List<String> readData(String value) {
        if (TextUtils.isEmpty(value))
            return null;

        data += value;
        if (data.startsWith(Constants.COMMAND_ROBOT_PREFIX)
                && data.endsWith(Constants.COMMAND_ROBOT_SUFFIX)) {
            //存在同时接收到多条的情况
            String[] split = data.split(Constants.COMMAND_ROBOT_PREFIX);
            if (split.length > 2) {
                data = Constants.EMPTY_STRING;
                List<String> result = new ArrayList<>();
                for (String s : split) {
                    if (s.contains(Constants.COMMAND_ROBOT_SUFFIX)) {
                        String substring = s.substring(0, s.length() - Constants.COMMAND_ROBOT_SUFFIX.length());
                        LogUtils.d(SocketService.TAG, "completion :" + substring);
                        result.add(substring);
                    }
                }
                return result;
            } else {
                String result = data.substring(Constants.COMMAND_ROBOT_PREFIX.length(), data.length() - Constants.COMMAND_ROBOT_SUFFIX.length());
                data = Constants.EMPTY_STRING;
                LogUtils.d(SocketService.TAG, "completion :" + result);
                return Collections.singletonList(result);
            }
        } else if (data.contains(Constants.COMMAND_ROBOT_SUFFIX)) {
            data = Constants.EMPTY_STRING;
            return null;
        } else
            return null;
    }

    public synchronized void clearData() {
        data = Constants.EMPTY_STRING;
    }
}
