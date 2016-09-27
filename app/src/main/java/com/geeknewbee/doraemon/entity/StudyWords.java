package com.geeknewbee.doraemon.entity;

import java.util.ArrayList;

/**
 * Created by GYY on 2016/9/22.
 */
public class StudyWords {

    public int getWid() {
        return wid;
    }

    public void setWid(int wid) {
        this.wid = wid;
    }

    /**
     * begin : 0
     * end : 5
     * words : ["bag","book","calculator","dictionary","eraser"]
     */

    private int wid;

    private ArrayList<String> words;
    /**
     * story :
     */

    private String story;


    public ArrayList<String> getWords() {
        return words;
    }

    public void setWords(ArrayList<String> words) {
        this.words = words;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }
}
