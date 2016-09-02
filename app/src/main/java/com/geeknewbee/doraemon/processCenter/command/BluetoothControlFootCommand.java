package com.geeknewbee.doraemon.processcenter.command;

/**
 * 蓝牙手动操作脚步
 */
public class BluetoothControlFootCommand extends Command {
    public int v;
    public int w;

    public BluetoothControlFootCommand(int v, int w) {
        super(CommandType.BLUETOOTH_CONTROL_FOOT);
        this.v = v;
        this.w = w;
    }
}
