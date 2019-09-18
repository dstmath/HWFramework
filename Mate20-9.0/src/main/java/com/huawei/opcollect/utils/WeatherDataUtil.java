package com.huawei.opcollect.utils;

import com.huawei.opcollect.weather.HwWeatherData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataUtil {
    private static final String TAG = WeatherDataUtil.class.getSimpleName();

    public static HwWeatherData parserWeather(String str) {
        if (str != null) {
            try {
                if (!"".equals(str)) {
                    JSONArray weatherData = (JSONArray) new JSONObject(str).get("weather");
                    if (weatherData == null || weatherData.length() <= 0) {
                        OPCollectLog.e(TAG, "weather data is null!");
                        return null;
                    }
                    JSONObject mWeatherObject = (JSONObject) weatherData.opt(0);
                    if (mWeatherObject == null) {
                        OPCollectLog.e(TAG, "weather object is null!");
                        return null;
                    }
                    HwWeatherData hwWeatherData = new HwWeatherData();
                    hwWeatherData.setCurrent_temperature(mWeatherObject.getInt("current_temperature"));
                    hwWeatherData.setWeather_icon(mWeatherObject.getInt("weather_icon"));
                    return hwWeatherData;
                }
            } catch (JSONException e) {
                OPCollectLog.e(TAG, "e:" + e.getMessage());
                return null;
            }
        }
        OPCollectLog.e(TAG, "str is null or empty.");
        return null;
    }
}
