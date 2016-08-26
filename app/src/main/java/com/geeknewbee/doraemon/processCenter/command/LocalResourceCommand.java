package com.geeknewbee.doraemon.processcenter.command;

import android.text.TextUtils;

import com.geeknewbee.doraemonsdk.BaseApplication;

public class LocalResourceCommand extends Command {
    public int resourceID;

    public LocalResourceCommand(int resourceID) {
        super(CommandType.PLAY_LOCAL_RESOURCE);
        this.resourceID = resourceID;
    }

    public LocalResourceCommand(String resourceName) {
        super(CommandType.PLAY_LOCAL_RESOURCE);
        if (!TextUtils.isEmpty(resourceName)) {
            int imageResId = BaseApplication.mContext.getResources().getIdentifier(resourceName, "raw", BaseApplication.mContext.getPackageName());
            this.resourceID = imageResId;
        }
    }
}
