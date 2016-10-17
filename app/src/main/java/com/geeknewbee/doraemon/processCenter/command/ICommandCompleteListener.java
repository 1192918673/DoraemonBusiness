package com.geeknewbee.doraemon.processcenter.command;

public interface ICommandCompleteListener {
    void onComplete(long id, boolean isSuccess, String error);
}
