package com.geeknewbee.doraemon.input.bluetooth;

import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemonsdk.utils.BytesUtils;

import java.util.Arrays;

public class SocketReader {
    private byte[] result;

    public SocketReader() {
    }

    public synchronized byte[] readData(byte[] bytes) {
        if (bytes == null || bytes.length < 1)
            return null;
        String receiveDataStr = new String(bytes, 0, Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET.length());
        //收到一个新开始的命令的时候 舍弃以前的数据
        if (receiveDataStr.equals(Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET))
            result = bytes;
        else
            result = BytesUtils.concat(result, bytes);

        String startStr = new String(result, 0, Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET.length());
        String endStr = new String(result, result.length - Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET.length(), Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET.length());
        if (startStr.equals(Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET) && endStr.equals(Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET)) {
            //存在同时接收到多条的情况
//            String readMessage = Constants.EMPTY_STRING;
//            try {
//                readMessage = new String(result, "ASCII");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            result = null;
//            String[] split = readMessage.split(Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET);
//            if (split.length > 2) {
//                List<String> result = new ArrayList<>();
//                for (String s : split) {
//                    if (s.contains(Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET)) {
//                        String substring = s.substring(0, s.length() - Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET.length());
//                        LogUtils.d(SocketService.TAG, "completion :" + substring);
//                        result.add(substring);
//                    }
//                }
//                return result;
//            } else {
//            String message = readMessage.substring(Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET.length(),
//                    readMessage.length() - Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET.length());
//            LogUtils.d(SocketService.TAG, "completion :" + message);
            return Arrays.copyOfRange(result, Constants.COMMAND_ROBOT_PREFIX_FOR_SOCKET.getBytes().length,
                    result.length - Constants.COMMAND_ROBOT_SUFFIX_FOR_SOCKET.getBytes().length);
//            }
        } else {
            return null;
        }
    }

    public synchronized void clearData() {
        result = null;
    }
}
