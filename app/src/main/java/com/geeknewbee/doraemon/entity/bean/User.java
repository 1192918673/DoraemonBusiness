package com.geeknewbee.doraemon.entity.bean;

/**
 * Created by mac on 16/7/4.
 */
public class User {
    private String user_id;
    private String personId;
    private String name;
    private String age;
    private String gender;

    public User(String personId, String name, String age, String gender) {
        this.personId = personId;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public User() {

    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
