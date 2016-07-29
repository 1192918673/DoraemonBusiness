package com.geeknewbee.doraemon.http;


import com.geeknewbee.doraemon.constants.Constants;

public class BaseResponseBody<T> {
    protected int code = 0;
    protected String msg = "";
    protected T data;

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public boolean isSuccess() {
        return code == Constants.RESPONSE_STATUS_SUCCESS;
    }
}
