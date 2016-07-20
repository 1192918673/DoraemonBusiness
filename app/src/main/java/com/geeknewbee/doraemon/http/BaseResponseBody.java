package com.geeknewbee.doraemon.http;


import com.geeknewbee.doraemon.util.Constant;

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
        return code == Constant.RESPONSE_STATUS_SUCCESS;
    }
}
