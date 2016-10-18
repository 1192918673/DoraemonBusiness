package com.geeknewbee.doraemon.weather;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.geeknewbee.doraemon.App;
import com.geeknewbee.doraemon.constants.Constants;
import com.geeknewbee.doraemon.entity.Location;
import com.geeknewbee.doraemon.entity.WeatherResponse;
import com.geeknewbee.doraemon.webservice.ApiService;
import com.geeknewbee.doraemon.webservice.RetrofitUtils;
import com.geeknewbee.doraemonsdk.utils.LogUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 根据当前位置查询天气
 */
public class WeatherManager {
    public static final String TAG = WeatherManager.class.getSimpleName();
    public static volatile WeatherManager instance;
    private AMapLocationClient locationClient;
    private Location currentLocation;
    private WeatherResponse.Weather currentWeather;

    private WeatherManager() {
    }

    public static WeatherManager getInstance() {
        if (instance == null) {
            synchronized (WeatherManager.class) {
                if (instance == null) {
                    instance = new WeatherManager();
                }
            }
        }
        return instance;
    }

    public WeatherResponse.Weather getWeatherReport() {
        if (currentWeather == null) {
            if (currentLocation == null) {
                startLocation();
            } else {
                queryWeather();
            }

            return null;
        } else
            return currentWeather;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    private void queryWeather() {
        Retrofit retrofit = RetrofitUtils.getRetrofit(Constants.GAO_DE_WEATHER_URL);

        ApiService service = retrofit.create(ApiService.class);
        service.get_weather(Constants.GAO_DE_WEATHER_KEY, currentLocation.getCity()).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response != null && response.isSuccessful() && response.body().getStatus().equals("1")) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse.getLives().size() == 0)
                        currentWeather = weatherResponse.getLives().get(0);
                    else {
                        for (WeatherResponse.Weather weather : weatherResponse.getLives()) {
                            if ((weather.getProvince().contains(currentLocation.getProvince()) || currentLocation.getProvince().contains(weather.getProvince()))
                                    && (weather.getCity().contains(currentLocation.getCity()) || currentLocation.getCity().contains(weather.getCity()))) {
                                currentWeather = weather;
                                break;
                            }
                        }
                    }
                    LogUtils.d(TAG, "query weather success");
                } else
                    LogUtils.d(TAG, "query weather failure");
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                LogUtils.d(TAG, "query weather failure:" + t.getMessage());
            }
        });
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
        stopLocation();
        initLocation();
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        if (locationClient != null)
            locationClient.stopLocation();
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    public void destroy() {
        stopLocation();

        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
        }
    }

    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(App.mContext);
        //设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是ture
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                currentLocation = getLocation(loc);
                queryWeather();
            } else {
                LogUtils.d(TAG, "定位失败，loc is null");
            }
        }
    };

    /**
     * 根据定位结果返回定位信息的字符串
     *
     * @param location
     * @return
     */
    public synchronized Location getLocation(AMapLocation location) {
        if (null == location) {
            return null;
        }
        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if (location.getErrorCode() == 0) {
            LogUtils.d(TAG, "定位成功");
            if (location.getProvider().equalsIgnoreCase(
                    android.location.LocationManager.GPS_PROVIDER)) {
                LogUtils.d(TAG, "GPS 定位");
                return null;
            } else {
                stopLocation();//获取到位置后就停止定位
                return new Location(location.getProvince(), location.getCity());
            }
        } else {
            //定位失败
            LogUtils.d(TAG, "定位失败:"
                    + "错误码:" + location.getErrorCode() + "\n"
                    + "错误信息:" + location.getErrorInfo() + "\n"
                    + "错误描述:" + location.getLocationDetail() + "\n");
            return null;
        }
    }
}
