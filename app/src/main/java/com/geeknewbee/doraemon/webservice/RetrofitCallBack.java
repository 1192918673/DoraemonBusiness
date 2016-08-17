package com.geeknewbee.doraemon.webservice;

public interface RetrofitCallBack<T> {
    void onSuccess(T response);

    void onFailure(String error);
}
