package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherAlarmModelHelper extends AEntityHelper<WeatherAlarmModel> {
    private static final WeatherAlarmModelHelper INSTANCE = new WeatherAlarmModelHelper();

    private WeatherAlarmModelHelper() {
    }

    public static WeatherAlarmModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, WeatherAlarmModel object) {
        Long _id = object.get_id();
        if (_id != null) {
            statement.bindLong(1, _id.longValue());
        } else {
            statement.bindNull(1);
        }
        statement.bindLong(2, object.getWeather_id());
        String alarm_id = object.getAlarm_id();
        if (alarm_id != null) {
            statement.bindString(3, alarm_id);
        } else {
            statement.bindNull(3);
        }
        String province_name = object.getProvince_name();
        if (province_name != null) {
            statement.bindString(4, province_name);
        } else {
            statement.bindNull(4);
        }
        String city_name = object.getCity_name();
        if (city_name != null) {
            statement.bindString(5, city_name);
        } else {
            statement.bindNull(5);
        }
        String county_name = object.getCounty_name();
        if (county_name != null) {
            statement.bindString(6, county_name);
        } else {
            statement.bindNull(6);
        }
        statement.bindLong(7, (long) object.getAlarm_type());
        String alarm_type_name = object.getAlarm_type_name();
        if (alarm_type_name != null) {
            statement.bindString(8, alarm_type_name);
        } else {
            statement.bindNull(8);
        }
        statement.bindLong(9, (long) object.getLevel());
        String level_name = object.getLevel_name();
        if (level_name != null) {
            statement.bindString(10, level_name);
        } else {
            statement.bindNull(10);
        }
        statement.bindLong(11, object.getObservationtime());
        String alarm_content = object.getAlarm_content();
        if (alarm_content != null) {
            statement.bindString(12, alarm_content);
        } else {
            statement.bindNull(12);
        }
    }

    public WeatherAlarmModel readObject(Cursor cursor, int offset) {
        return new WeatherAlarmModel(cursor);
    }

    public void setPrimaryKeyValue(WeatherAlarmModel object, long value) {
        object.set_id(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, WeatherAlarmModel object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
