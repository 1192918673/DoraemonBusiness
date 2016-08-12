package com.geeknewbee.doraemon.entity;

/**
 * 天气查询结果实体类
 */
public class WeatherEntity {
    public String code;
    public data data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public WeatherEntity.data getData() {

        return data;
    }

    public void setData(WeatherEntity.data data) {
        this.data = data;
    }

    public class data {
        public api api;
        public city city;
        public condition condition;

        public WeatherEntity.data.api getApi() {
            return api;
        }

        public void setApi(WeatherEntity.data.api api) {
            this.api = api;
        }

        public WeatherEntity.data.city getCity() {
            return city;
        }

        public void setCity(WeatherEntity.data.city city) {
            this.city = city;
        }

        public WeatherEntity.data.condition getCondition() {
            return condition;
        }

        public void setCondition(WeatherEntity.data.condition condition) {
            this.condition = condition;
        }

        public class api {
            public String cityName;
            public String pubTime;
            public String value;

            public String getCityName() {
                return cityName;
            }

            public void setCityName(String cityName) {
                this.cityName = cityName;
            }

            public String getPubTime() {
                return pubTime;
            }

            public void setPubTime(String pubTime) {
                this.pubTime = pubTime;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        public class city {
            public String cityId;
            public String counname;
            public String name;
            public String pname;

            public String getCityId() {

                return cityId;
            }

            public void setCityId(String cityId) {
                this.cityId = cityId;
            }

            public String getCounname() {
                return counname;
            }

            public void setCounname(String counname) {
                this.counname = counname;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPname() {
                return pname;
            }

            public void setPname(String pname) {
                this.pname = pname;
            }
        }

        public class condition {
            public String condition;
            public String conditionId;
            public String humidity;
            public String icon;
            public String pressure;
            public String realFeel;
            public String sunRise;
            public String sunSet;
            public String temp;
            public String tips;
            public String updatetime;
            public String uvi;
            public String windDir;
            public String windLevel;
            public String windSpeed;

            public String getCondition() {

                return condition;
            }

            public void setCondition(String condition) {
                this.condition = condition;
            }

            public String getConditionId() {
                return conditionId;
            }

            public void setConditionId(String conditionId) {
                this.conditionId = conditionId;
            }

            public String getHumidity() {
                return humidity;
            }

            public void setHumidity(String humidity) {
                this.humidity = humidity;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public String getPressure() {
                return pressure;
            }

            public void setPressure(String pressure) {
                this.pressure = pressure;
            }

            public String getRealFeel() {
                return realFeel;
            }

            public void setRealFeel(String realFeel) {
                this.realFeel = realFeel;
            }

            public String getSunRise() {
                return sunRise;
            }

            public void setSunRise(String sunRise) {
                this.sunRise = sunRise;
            }

            public String getSunSet() {
                return sunSet;
            }

            public void setSunSet(String sunSet) {
                this.sunSet = sunSet;
            }

            public String getTemp() {
                return temp;
            }

            public void setTemp(String temp) {
                this.temp = temp;
            }

            public String getTips() {
                return tips;
            }

            public void setTips(String tips) {
                this.tips = tips;
            }

            public String getUpdatetime() {
                return updatetime;
            }

            public void setUpdatetime(String updatetime) {
                this.updatetime = updatetime;
            }

            public String getUvi() {
                return uvi;
            }

            public void setUvi(String uvi) {
                this.uvi = uvi;
            }

            public String getWindDir() {
                return windDir;
            }

            public void setWindDir(String windDir) {
                this.windDir = windDir;
            }

            public String getWindLevel() {
                return windLevel;
            }

            public void setWindLevel(String windLevel) {
                this.windLevel = windLevel;
            }

            public String getWindSpeed() {
                return windSpeed;
            }

            public void setWindSpeed(String windSpeed) {
                this.windSpeed = windSpeed;
            }
        }
    }
}
