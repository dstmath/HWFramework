package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherAlarmModelHelper extends AEntityHelper<WeatherAlarmModel> {
    private static final WeatherAlarmModelHelper INSTANCE = new WeatherAlarmModelHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, WeatherAlarmModel weatherAlarmModel) {
        return null;
    }

    private WeatherAlarmModelHelper() {
    }

    public static WeatherAlarmModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, WeatherAlarmModel weatherAlarmModel) {
        Long l = weatherAlarmModel.get_id();
        if (l != null) {
            statement.bindLong(1, l.longValue());
        } else {
            statement.bindNull(1);
        }
        statement.bindLong(2, weatherAlarmModel.getWeather_id());
        String alarm_id = weatherAlarmModel.getAlarm_id();
        if (alarm_id != null) {
            statement.bindString(3, alarm_id);
        } else {
            statement.bindNull(3);
        }
        String province_name = weatherAlarmModel.getProvince_name();
        if (province_name != null) {
            statement.bindString(4, province_name);
        } else {
            statement.bindNull(4);
        }
        String city_name = weatherAlarmModel.getCity_name();
        if (city_name != null) {
            statement.bindString(5, city_name);
        } else {
            statement.bindNull(5);
        }
        String county_name = weatherAlarmModel.getCounty_name();
        if (county_name != null) {
            statement.bindString(6, county_name);
        } else {
            statement.bindNull(6);
        }
        statement.bindLong(7, (long) weatherAlarmModel.getAlarm_type());
        String alarm_type_name = weatherAlarmModel.getAlarm_type_name();
        if (alarm_type_name != null) {
            statement.bindString(8, alarm_type_name);
        } else {
            statement.bindNull(8);
        }
        statement.bindLong(9, (long) weatherAlarmModel.getLevel());
        String level_name = weatherAlarmModel.getLevel_name();
        if (level_name != null) {
            statement.bindString(10, level_name);
        } else {
            statement.bindNull(10);
        }
        statement.bindLong(11, weatherAlarmModel.getObservationtime());
        String alarm_content = weatherAlarmModel.getAlarm_content();
        if (alarm_content != null) {
            statement.bindString(12, alarm_content);
        } else {
            statement.bindNull(12);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public WeatherAlarmModel readObject(Cursor cursor, int i) {
        return new WeatherAlarmModel(cursor);
    }

    public void setPrimaryKeyValue(WeatherAlarmModel weatherAlarmModel, long j) {
        weatherAlarmModel.set_id(Long.valueOf(j));
    }
}
