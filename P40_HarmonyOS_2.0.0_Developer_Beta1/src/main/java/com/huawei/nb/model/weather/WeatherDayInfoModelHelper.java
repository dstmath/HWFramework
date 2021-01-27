package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherDayInfoModelHelper extends AEntityHelper<WeatherDayInfoModel> {
    private static final WeatherDayInfoModelHelper INSTANCE = new WeatherDayInfoModelHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, WeatherDayInfoModel weatherDayInfoModel) {
        return null;
    }

    private WeatherDayInfoModelHelper() {
    }

    public static WeatherDayInfoModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, WeatherDayInfoModel weatherDayInfoModel) {
        Long l = weatherDayInfoModel.get_id();
        if (l != null) {
            statement.bindLong(1, l.longValue());
        } else {
            statement.bindNull(1);
        }
        statement.bindLong(2, weatherDayInfoModel.getWeather_info_id());
        statement.bindLong(3, (long) weatherDayInfoModel.getDay_index());
        statement.bindLong(4, weatherDayInfoModel.getObs_date());
        String day_code = weatherDayInfoModel.getDay_code();
        if (day_code != null) {
            statement.bindString(5, day_code);
        } else {
            statement.bindNull(5);
        }
        statement.bindLong(6, weatherDayInfoModel.getSun_rise_time());
        statement.bindLong(7, weatherDayInfoModel.getSun_set_time());
        statement.bindString(8, Float.toString(weatherDayInfoModel.getHigh_temp()));
        statement.bindString(9, Float.toString(weatherDayInfoModel.getLow_temp()));
        statement.bindLong(10, (long) weatherDayInfoModel.getWeather_icon());
        statement.bindLong(11, (long) weatherDayInfoModel.getWind_speed());
        String wind_direction = weatherDayInfoModel.getWind_direction();
        if (wind_direction != null) {
            statement.bindString(12, wind_direction);
        } else {
            statement.bindNull(12);
        }
        String text_short = weatherDayInfoModel.getText_short();
        if (text_short != null) {
            statement.bindString(13, text_short);
        } else {
            statement.bindNull(13);
        }
        String text_long = weatherDayInfoModel.getText_long();
        if (text_long != null) {
            statement.bindString(14, text_long);
        } else {
            statement.bindNull(14);
        }
        statement.bindString(15, Float.toString(weatherDayInfoModel.getNight_high_temp()));
        statement.bindString(16, Float.toString(weatherDayInfoModel.getNight_low_temp()));
        statement.bindLong(17, (long) weatherDayInfoModel.getNight_weather_icon());
        statement.bindLong(18, (long) weatherDayInfoModel.getNight_wind_speed());
        String night_wind_direction = weatherDayInfoModel.getNight_wind_direction();
        if (night_wind_direction != null) {
            statement.bindString(19, night_wind_direction);
        } else {
            statement.bindNull(19);
        }
        String night_text_short = weatherDayInfoModel.getNight_text_short();
        if (night_text_short != null) {
            statement.bindString(20, night_text_short);
        } else {
            statement.bindNull(20);
        }
        String night_text_long = weatherDayInfoModel.getNight_text_long();
        if (night_text_long != null) {
            statement.bindString(21, night_text_long);
        } else {
            statement.bindNull(21);
        }
        statement.bindLong(22, (long) weatherDayInfoModel.getMoon_type());
        String mobile_link = weatherDayInfoModel.getMobile_link();
        if (mobile_link != null) {
            statement.bindString(23, mobile_link);
        } else {
            statement.bindNull(23);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public WeatherDayInfoModel readObject(Cursor cursor, int i) {
        return new WeatherDayInfoModel(cursor);
    }

    public void setPrimaryKeyValue(WeatherDayInfoModel weatherDayInfoModel, long j) {
        weatherDayInfoModel.set_id(Long.valueOf(j));
    }
}
