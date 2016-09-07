package com.geeknewbee.doraemon.entity.event;

/**
 * Created by GYY on 2016/9/6.
 */
public class BLLocalResponse {
    /**
     * code : 0
     * msg : network init  success
     */

    private int code;
    private String msg;

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
