package com.geeknewbee.doraemon.BL;

import java.util.HashMap;
import java.util.Map;

/**
 * 构造BroadLink的命令
 * Created by GYY on 2016/9/6.
 */
public class BLLocalCMD {
    /**
     * api_id : 134
     * command : rm2_send
     */

    private int api_id;
    private String command;
    private HashMap<String, Object> params = new HashMap<String, Object>(10);

    public void setApi_id(int api_id) {
        this.api_id = api_id;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getApi_id() {
        return api_id;
    }

    public String getCommand() {
        return command;
    }

    public void put(String key, Object value) {
        params.put(key, value);
    }

    public void clearParams() {
        params.clear();
    }

    public String getCMDString() {
        String ret = "{" +
                "\"api_id\":" + api_id + "," +
                "\"command\":\"" + command + "\"";
        for (Map.Entry entry :
                params.entrySet()) {
            ret += ",\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"";
        }
        ret += "}";
        return ret;
    }
}
