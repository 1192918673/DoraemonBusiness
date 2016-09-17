package com.geeknewbee.doraemon.output.action;

import com.geeknewbee.doraemon.jni.SerialPort;

import java.io.File;
import java.io.FileDescriptor;

/**
 * 山东公司实现中控板
 */
public class SDArmsAndHead implements IArmsAndHead {
    private SerialPort port1;

    @Override
    public boolean init() {
        boolean result = false;
        File device = new File("/dev/ttyHS0");
        if (device.canRead() && device.canWrite()) {
            port1 = new SerialPort();
            FileDescriptor mFd = port1.open(device.getAbsolutePath(), 115200, 0);
            if (mFd == null) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }

    @Override
    public synchronized boolean send(byte code, char[] buf) {
        if (port1 != null) {
//            char crc = port1.getCrc(code, buf, buf.length);
//            char[] bytes = charToByte(crc);
            return port1.send(code, buf, buf.length) > 0;
        }
        return false;
    }

    private char[] charToByte(char c) {
        char[] b = new char[2];
        b[0] = (char) ((c & 0xFF00) >> 8);
        b[1] = (char) (c & 0xFF);
        return b;
    }

    public void reset() {
        char[] buf = new char[]{0x06, 0x01, 0x00, 0x02, 0x00, 0x03, 0x00, 0x04, 0x00, 0x05, 0x00, 0x06, 0x00, 0x03, 0xe8, 0x00, 0x00};
        send((byte) 0x02, buf);
    }
}
