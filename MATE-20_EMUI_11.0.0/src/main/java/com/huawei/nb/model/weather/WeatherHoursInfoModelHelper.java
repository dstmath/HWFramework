package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherHoursInfoModelHelper extends AEntityHelper<WeatherHoursInfoModel> {
    private static final WeatherHoursInfoModelHelper INSTANCE = new WeatherHoursInfoModelHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, WeatherHoursInfoModel weatherHoursInfoModel) {
        return null;
    }

    private WeatherHoursInfoModelHelper() {
    }

    public static WeatherHoursInfoModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, WeatherHoursInfoModel weatherHoursInfoModel) {
        Long l = weatherHoursInfoModel.get_id();
        if (l != null) {
            statement.bindLong(1, l.longValue());
        } else {
            statement.bindNull(1);
        }
        statement.bindLong(2, weatherHoursInfoModel.getWeather_id());
        statement.bindLong(3, weatherHoursInfoModel.getForcase_date_time());
        statement.bindLong(4, (long) weatherHoursInfoModel.getWeather_icon());
        statement.bindString(5, Float.toString(weatherHoursInfoModel.getHour_temprature()));
        statement.bindLong(6, (long) weatherHoursInfoModel.getIs_day_light());
        statement.bindString(7, Float.toString(weatherHoursInfoModel.getRain_probability()));
        String mobile_link = weatherHoursInfoModel.getMobile_link();
        if (mobile_link != null) {
            statement.bindString(8, mobile_link);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public WeatherHoursInfoModel readObject(Cursor cursor, int i) {
        return new WeatherHoursInfoModel(cursor);
    }

    public void setPrimaryKeyValue(WeatherHoursInfoModel weatherHoursInfoModel, long j) {
        weatherHoursInfoModel.set_id(Long.valueOf(j));
    }
}
