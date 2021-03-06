package com.geeknewbee.doraemon.database;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "WEATHER__CITY".
 */
public class Weather_City {

    private Long id;
    private String cityId;
    private String name;
    private String nameEn;
    private String namePy;
    private String province;
    private String weatherCnId;

    /**
     * Used to resolve relations
     */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient Weather_CityDao myDao;


    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Weather_City() {
    }

    public Weather_City(Long id) {
        this.id = id;
    }

    public Weather_City(Long id, String cityId, String name, String nameEn, String namePy, String province, String weatherCnId) {
        this.id = id;
        this.cityId = cityId;
        this.name = name;
        this.nameEn = nameEn;
        this.namePy = namePy;
        this.province = province;
        this.weatherCnId = weatherCnId;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getWeather_CityDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNamePy() {
        return namePy;
    }

    public void setNamePy(String namePy) {
        this.namePy = namePy;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getWeatherCnId() {
        return weatherCnId;
    }

    public void setWeatherCnId(String weatherCnId) {
        this.weatherCnId = weatherCnId;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
