package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherInfoModelHelper extends AEntityHelper<WeatherInfoModel> {
    private static final WeatherInfoModelHelper INSTANCE = new WeatherInfoModelHelper();

    private WeatherInfoModelHelper() {
    }

    public static WeatherInfoModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, WeatherInfoModel object) {
        Long _id = object.get_id();
        if (_id != null) {
            statement.bindLong(1, _id.longValue());
        } else {
            statement.bindNull(1);
        }
        statement.bindLong(2, (long) object.getStatus());
        String city_code = object.getCity_code();
        if (city_code != null) {
            statement.bindString(3, city_code);
        } else {
            statement.bindNull(3);
        }
        String time_zone = object.getTime_zone();
        if (time_zone != null) {
            statement.bindString(4, time_zone);
        } else {
            statement.bindNull(4);
        }
        statement.bindLong(5, object.getUpdate_time());
        statement.bindLong(6, (long) object.getIsday_light());
        statement.bindString(7, Float.toString(object.getTemperature()));
        statement.bindLong(8, (long) object.getWeather_icon());
        String weather_text = object.getWeather_text();
        if (weather_text != null) {
            statement.bindString(9, weather_text);
        } else {
            statement.bindNull(9);
        }
        statement.bindLong(10, object.getObservation_time());
        statement.bindLong(11, (long) object.getWind_speed());
        String wind_direction = object.getWind_direction();
        if (wind_direction != null) {
            statement.bindString(12, wind_direction);
        } else {
            statement.bindNull(12);
        }
        statement.bindLong(13, (long) object.getP_num());
        String p_status_cn = object.getP_status_cn();
        if (p_status_cn != null) {
            statement.bindString(14, p_status_cn);
        } else {
            statement.bindNull(14);
        }
        String p_status_en = object.getP_status_en();
        if (p_status_en != null) {
            statement.bindString(15, p_status_en);
        } else {
            statement.bindNull(15);
        }
        statement.bindString(16, Float.toString(object.getPm10()));
        statement.bindString(17, Float.toString(object.getPm2_5()));
        statement.bindString(18, Float.toString(object.getNo2()));
        statement.bindString(19, Float.toString(object.getSo2()));
        statement.bindString(20, Float.toString(object.getO3()));
        statement.bindString(21, Float.toString(object.getCo()));
        String p_desc_en = object.getP_desc_en();
        if (p_desc_en != null) {
            statement.bindString(22, p_desc_en);
        } else {
            statement.bindNull(22);
        }
        String p_desc_cn = object.getP_desc_cn();
        if (p_desc_cn != null) {
            statement.bindString(23, p_desc_cn);
        } else {
            statement.bindNull(23);
        }
        String humidity = object.getHumidity();
        if (humidity != null) {
            statement.bindString(24, humidity);
        } else {
            statement.bindNull(24);
        }
        statement.bindString(25, Float.toString(object.getRealfeel()));
        String mobile_link = object.getMobile_link();
        if (mobile_link != null) {
            statement.bindString(26, mobile_link);
        } else {
            statement.bindNull(26);
        }
        String ninety_mobile_link = object.getNinety_mobile_link();
        if (ninety_mobile_link != null) {
            statement.bindString(27, ninety_mobile_link);
        } else {
            statement.bindNull(27);
        }
        statement.bindLong(28, (long) object.getUv_index());
        statement.bindString(29, Float.toString(object.getAir_pressure()));
    }

    public WeatherInfoModel readObject(Cursor cursor, int offset) {
        return new WeatherInfoModel(cursor);
    }

    public void setPrimaryKeyValue(WeatherInfoModel object, long value) {
        object.set_id(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, WeatherInfoModel object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
