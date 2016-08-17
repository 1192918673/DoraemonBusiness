package com.geeknewbee.doraemon.entity;

import java.util.List;

public class GetAnswerResponse {

    /**
     * answer : 你好才好
     */

    private String answer;
    /**
     * type : 1
     */

    private int type;
    /**
     * data :
     */

    private String data;
    /**
     * action : ["head_up","head_down"]
     * expression : ["http://doraemon.microfastup.com/media/expression/photos/2016/07/18/c42d1184622e45a88fac3c9f1ba06788.jpg","http://doraemon.microfastup.com/media/expression/photos/2016/07/18/247988466a274471ae76bf35a1bdc1c0.jpg","http://doraemon.microfastup.com/media/expression/photos/2016/07/18/fa5205b7862d4ffba013bd076786cc68.jpg"]
     * interval : 100
     */

    private int interval;
    private List<String> action;
    private List<String> expression;
    /**
     * local_resource :
     */

    private String local_resource;

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public List<String> getExpression() {
        return expression;
    }

    public void setExpression(List<String> expression) {
        this.expression = expression;
    }

    public String getLocal_resource() {
        return local_resource;
    }

    public void setLocal_resource(String local_resource) {
        this.local_resource = local_resource;
    }
}
