package com.geeknewbee.doraemon.entity;

/**
 * 请求最新AppVersion响应实体
 */
public class ResponseAppVersion {

    /**
     * apk : http://doraemon.microfastup.com/media/apk/mobileqq_android.apk
     * last_version_code : 350
     */

    private String apk;
    private int last_version_code;

    public String getApk() {
        return apk;
    }

    public void setApk(String apk) {
        this.apk = apk;
    }

    public int getLast_version_code() {
        return last_version_code;
    }

    public void setLast_version_code(int last_version_code) {
        this.last_version_code = last_version_code;
    }
}
