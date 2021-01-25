package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherInfoModelHelper extends AEntityHelper<WeatherInfoModel> {
    private static final WeatherInfoModelHelper INSTANCE = new WeatherInfoModelHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, WeatherInfoModel weatherInfoModel) {
        return null;
    }

    private WeatherInfoModelHelper() {
    }

    public static WeatherInfoModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, WeatherInfoModel weatherInfoModel) {
        Long l = weatherInfoModel.get_id();
        if (l != null) {
            statement.bindLong(1, l.longValue());
        } else {
            statement.bindNull(1);
        }
        statement.bindLong(2, (long) weatherInfoModel.getStatus());
        String city_code = weatherInfoModel.getCity_code();
        if (city_code != null) {
            statement.bindString(3, city_code);
        } else {
            statement.bindNull(3);
        }
        String time_zone = weatherInfoModel.getTime_zone();
        if (time_zone != null) {
            statement.bindString(4, time_zone);
        } else {
            statement.bindNull(4);
        }
        statement.bindLong(5, weatherInfoModel.getUpdate_time());
        statement.bindLong(6, (long) weatherInfoModel.getIsday_light());
        statement.bindString(7, Float.toString(weatherInfoModel.getTemperature()));
        statement.bindLong(8, (long) weatherInfoModel.getWeather_icon());
        String weather_text = weatherInfoModel.getWeather_text();
        if (weather_text != null) {
            statement.bindString(9, weather_text);
        } else {
            statement.bindNull(9);
        }
        statement.bindLong(10, weatherInfoModel.getObservation_time());
        statement.bindLong(11, (long) weatherInfoModel.getWind_speed());
        String wind_direction = weatherInfoModel.getWind_direction();
        if (wind_direction != null) {
            statement.bindString(12, wind_direction);
        } else {
            statement.bindNull(12);
        }
        statement.bindLong(13, (long) weatherInfoModel.getP_num());
        String p_status_cn = weatherInfoModel.getP_status_cn();
        if (p_status_cn != null) {
            statement.bindString(14, p_status_cn);
        } else {
            statement.bindNull(14);
        }
        String p_status_en = weatherInfoModel.getP_status_en();
        if (p_status_en != null) {
            statement.bindString(15, p_status_en);
        } else {
            statement.bindNull(15);
        }
        statement.bindString(16, Float.toString(weatherInfoModel.getPm10()));
        statement.bindString(17, Float.toString(weatherInfoModel.getPm2_5()));
        statement.bindString(18, Float.toString(weatherInfoModel.getNo2()));
        statement.bindString(19, Float.toString(weatherInfoModel.getSo2()));
        statement.bindString(20, Float.toString(weatherInfoModel.getO3()));
        statement.bindString(21, Float.toString(weatherInfoModel.getCo()));
        String p_desc_en = weatherInfoModel.getP_desc_en();
        if (p_desc_en != null) {
            statement.bindString(22, p_desc_en);
        } else {
            statement.bindNull(22);
        }
        String p_desc_cn = weatherInfoModel.getP_desc_cn();
        if (p_desc_cn != null) {
            statement.bindString(23, p_desc_cn);
        } else {
            statement.bindNull(23);
        }
        String humidity = weatherInfoModel.getHumidity();
        if (humidity != null) {
            statement.bindString(24, humidity);
        } else {
            statement.bindNull(24);
        }
        statement.bindString(25, Float.toString(weatherInfoModel.getRealfeel()));
        String mobile_link = weatherInfoModel.getMobile_link();
        if (mobile_link != null) {
            statement.bindString(26, mobile_link);
        } else {
            statement.bindNull(26);
        }
        String ninety_mobile_link = weatherInfoModel.getNinety_mobile_link();
        if (ninety_mobile_link != null) {
            statement.bindString(27, ninety_mobile_link);
        } else {
            statement.bindNull(27);
        }
        statement.bindLong(28, (long) weatherInfoModel.getUv_index());
        statement.bindString(29, Float.toString(weatherInfoModel.getAir_pressure()));
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public WeatherInfoModel readObject(Cursor cursor, int i) {
        return new WeatherInfoModel(cursor);
    }

    public void setPrimaryKeyValue(WeatherInfoModel weatherInfoModel, long j) {
        weatherInfoModel.set_id(Long.valueOf(j));
    }
}
