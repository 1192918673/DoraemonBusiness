package com.geeknewbee.doraemon.processcenter.command;

public interface ICommandCompleteListener {
    void onTTSComplete(long id, boolean isSuccess, String error);
}
