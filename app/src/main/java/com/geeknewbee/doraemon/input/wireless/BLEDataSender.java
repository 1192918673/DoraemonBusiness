package com.geeknewbee.doraemon.input.wireless;

import android.text.TextUtils;

import com.geeknewbee.doraemon.constants.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BLE数据发送对象
 */
public class BLEDataSender {
    public static final int MAX_LENGTH = 18;

    /**
     * 获取分包
     *
     * @param data
     * @return
     */
    public static List<byte[]> getDataArray(String data) {
        if (TextUtils.isEmpty(data))
            return null;

        data = Constants.COMMAND_ROBOT_PREFIX + data + Constants.COMMAND_ROBOT_SUFFIX;

        List<byte[]> result = new ArrayList<>();
        byte[] bytes = data.getBytes();
        int length = bytes.length;
        int number = length % MAX_LENGTH == 0 ? length / MAX_LENGTH : length / MAX_LENGTH + 1;
        for (int i = 0; i < number; i++) {
            byte[] range = Arrays.copyOfRange(bytes, i * 18, i == number - 1 ? length : (i + 1) * MAX_LENGTH);
            result.add(range);
        }

        return result;
    }

}
