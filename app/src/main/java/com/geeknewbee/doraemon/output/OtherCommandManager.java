package com.geeknewbee.doraemon.output;

import android.content.Intent;

import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.input.SoundMonitorType;
import com.geeknewbee.doraemon.processcenter.Doraemon;
import com.geeknewbee.doraemon.processcenter.SyncQueue;
import com.geeknewbee.doraemon.processcenter.command.Command;
import com.geeknewbee.doraemon.processcenter.command.WifiCommand;

public class OtherCommandManager implements IOutput {
    public static volatile OtherCommandManager instance;

    private OtherCommandManager() {
    }

    public static OtherCommandManager getInstance() {
        if (instance == null) {
            synchronized (OtherCommandManager.class) {
                if (instance == null) {
                    instance = new OtherCommandManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void addCommand(Command command) {
        switch (command.getType()) {
            case STOP:
                SyncQueue.getInstance(App.mContext).stop();
                break;
            case TAKE_PICTURE:
                App.mContext.sendBroadcast(new Intent(Constants.READSENSE_BROADCAST_TAKE_PICTURE_ACTION));
                break;
            case SETTING_WIFI:
                WifiCommand wifiCommand = (WifiCommand) command;
                SysSettingManager.connectWiFi(wifiCommand.ssid, wifiCommand.pwd, wifiCommand.type);
                break;
            case SETTING_VOLUME:
                SysSettingManager.setVolume(command.getContent());
                break;
            case SLEEP:
                Doraemon.getInstance(App.mContext).switchSoundMonitor(SoundMonitorType.EDD);
                break;
        }
    }

    @Override
    public boolean isBusy() {
        //这些命令都是异步命令，随时可用
        return false;
    }

    @Override
    public void setBusy(boolean isBusy) {
        //该 输出通道一直都是非占用,随时可用
    }
}
