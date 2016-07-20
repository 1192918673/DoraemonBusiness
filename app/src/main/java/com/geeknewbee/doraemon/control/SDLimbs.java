package com.geeknewbee.doraemon.control;

import com.geeknewbee.doraemon.control.base.ILimbs;
import com.geeknewbee.doraemon.jni.SerialPort;

import java.io.File;
import java.io.FileDescriptor;

/**
 * 山东公司实现中控板
 */
public class SDLimbs implements ILimbs {
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
    public boolean send(LimbFunction limbFunctions, char[] buf) {
        if (port1 != null) {
            return port1.send(limbFunctions.getFunctionCode(), buf, buf.length) > 0;
        }
        return false;
    }

    @Override
    public boolean send(byte code, char[] buf) {
        if (port1 != null) {
            return port1.send(code, buf, buf.length) > 0;
        }
        return false;
    }
}
