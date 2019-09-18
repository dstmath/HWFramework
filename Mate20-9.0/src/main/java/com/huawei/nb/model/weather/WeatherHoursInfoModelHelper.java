package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherHoursInfoModelHelper extends AEntityHelper<WeatherHoursInfoModel> {
    private static final WeatherHoursInfoModelHelper INSTANCE = new WeatherHoursInfoModelHelper();

    private WeatherHoursInfoModelHelper() {
    }

    public static WeatherHoursInfoModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, WeatherHoursInfoModel object) {
        Long _id = object.get_id();
        if (_id != null) {
            statement.bindLong(1, _id.longValue());
        } else {
            statement.bindNull(1);
        }
        statement.bindLong(2, object.getWeather_id());
        statement.bindLong(3, object.getForcase_date_time());
        statement.bindLong(4, (long) object.getWeather_icon());
        statement.bindString(5, Float.toString(object.getHour_temprature()));
        statement.bindLong(6, (long) object.getIs_day_light());
        statement.bindString(7, Float.toString(object.getRain_probability()));
        String mobile_link = object.getMobile_link();
        if (mobile_link != null) {
            statement.bindString(8, mobile_link);
        } else {
            statement.bindNull(8);
        }
    }

    public WeatherHoursInfoModel readObject(Cursor cursor, int offset) {
        return new WeatherHoursInfoModel(cursor);
    }

    public void setPrimaryKeyValue(WeatherHoursInfoModel object, long value) {
        object.set_id(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, WeatherHoursInfoModel object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
