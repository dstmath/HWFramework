package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherDayInfoModelHelper extends AEntityHelper<WeatherDayInfoModel> {
    private static final WeatherDayInfoModelHelper INSTANCE = new WeatherDayInfoModelHelper();

    private WeatherDayInfoModelHelper() {
    }

    public static WeatherDayInfoModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, WeatherDayInfoModel object) {
        Long _id = object.get_id();
        if (_id != null) {
            statement.bindLong(1, _id.longValue());
        } else {
            statement.bindNull(1);
        }
        statement.bindLong(2, object.getWeather_info_id());
        statement.bindLong(3, (long) object.getDay_index());
        statement.bindLong(4, object.getObs_date());
        String day_code = object.getDay_code();
        if (day_code != null) {
            statement.bindString(5, day_code);
        } else {
            statement.bindNull(5);
        }
        statement.bindLong(6, object.getSun_rise_time());
        statement.bindLong(7, object.getSun_set_time());
        statement.bindString(8, Float.toString(object.getHigh_temp()));
        statement.bindString(9, Float.toString(object.getLow_temp()));
        statement.bindLong(10, (long) object.getWeather_icon());
        statement.bindLong(11, (long) object.getWind_speed());
        String wind_direction = object.getWind_direction();
        if (wind_direction != null) {
            statement.bindString(12, wind_direction);
        } else {
            statement.bindNull(12);
        }
        String text_short = object.getText_short();
        if (text_short != null) {
            statement.bindString(13, text_short);
        } else {
            statement.bindNull(13);
        }
        String text_long = object.getText_long();
        if (text_long != null) {
            statement.bindString(14, text_long);
        } else {
            statement.bindNull(14);
        }
        statement.bindString(15, Float.toString(object.getNight_high_temp()));
        statement.bindString(16, Float.toString(object.getNight_low_temp()));
        statement.bindLong(17, (long) object.getNight_weather_icon());
        statement.bindLong(18, (long) object.getNight_wind_speed());
        String night_wind_direction = object.getNight_wind_direction();
        if (night_wind_direction != null) {
            statement.bindString(19, night_wind_direction);
        } else {
            statement.bindNull(19);
        }
        String night_text_short = object.getNight_text_short();
        if (night_text_short != null) {
            statement.bindString(20, night_text_short);
        } else {
            statement.bindNull(20);
        }
        String night_text_long = object.getNight_text_long();
        if (night_text_long != null) {
            statement.bindString(21, night_text_long);
        } else {
            statement.bindNull(21);
        }
        statement.bindLong(22, (long) object.getMoon_type());
        String mobile_link = object.getMobile_link();
        if (mobile_link != null) {
            statement.bindString(23, mobile_link);
        } else {
            statement.bindNull(23);
        }
    }

    public WeatherDayInfoModel readObject(Cursor cursor, int offset) {
        return new WeatherDayInfoModel(cursor);
    }

    public void setPrimaryKeyValue(WeatherDayInfoModel object, long value) {
        object.set_id(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, WeatherDayInfoModel object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
