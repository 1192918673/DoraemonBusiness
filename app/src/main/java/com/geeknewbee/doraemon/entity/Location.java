package com.geeknewbee.doraemon.entity;

/**
 * 位置
 */
public class Location {
    private String province;
    private String city;

    public Location(String province, String city) {
        this.province = province;
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }
}
