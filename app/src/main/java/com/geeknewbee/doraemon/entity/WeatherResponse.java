package com.geeknewbee.doraemon.entity;

import java.util.List;

/**
 * 高德天气返回的天气数据
 */
public class WeatherResponse {

    /**
     * status : 1
     * count : 1
     * info : OK
     * infocode : 10000
     * lives : [{"province":"北京","city":"北京市","adcode":"110000","weather":"阴","temperature":"18","winddirection":"南","windpower":"5","humidity":"78","reporttime":"2016-10-18 16:00:00"}]
     */

    private String status;
    private String count;
    private String info;
    private String infocode;
    /**
     * province : 北京
     * city : 北京市
     * adcode : 110000
     * weather : 阴
     * temperature : 18
     * winddirection : 南
     * windpower : 5
     * humidity : 78
     * reporttime : 2016-10-18 16:00:00
     */

    private List<Weather> lives;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfocode() {
        return infocode;
    }

    public void setInfocode(String infocode) {
        this.infocode = infocode;
    }

    public List<Weather> getLives() {
        return lives;
    }

    public void setLives(List<Weather> lives) {
        this.lives = lives;
    }

    public static class Weather {
        private String province;
        private String city;
        private String adcode;
        private String weather;
        private String temperature;
        private String winddirection;
        private String windpower;
        private String humidity;
        private String reporttime;

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAdcode() {
            return adcode;
        }

        public void setAdcode(String adcode) {
            this.adcode = adcode;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getTemperature() {
            return temperature;
        }

        public void setTemperature(String temperature) {
            this.temperature = temperature;
        }

        public String getWinddirection() {
            return winddirection;
        }

        public void setWinddirection(String winddirection) {
            this.winddirection = winddirection;
        }

        public String getWindpower() {
            return windpower;
        }

        public void setWindpower(String windpower) {
            this.windpower = windpower;
        }

        public String getHumidity() {
            return humidity;
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }

        public String getReporttime() {
            return reporttime;
        }

        public void setReporttime(String reporttime) {
            this.reporttime = reporttime;
        }
    }
}
