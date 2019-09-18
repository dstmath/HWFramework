package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CityInfoModelHelper extends AEntityHelper<CityInfoModel> {
    private static final CityInfoModelHelper INSTANCE = new CityInfoModelHelper();

    private CityInfoModelHelper() {
    }

    public static CityInfoModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CityInfoModel object) {
        Long _id = object.get_id();
        if (_id != null) {
            statement.bindLong(1, _id.longValue());
        } else {
            statement.bindNull(1);
        }
        String city_name = object.getCity_name();
        if (city_name != null) {
            statement.bindString(2, city_name);
        } else {
            statement.bindNull(2);
        }
        String city_alias = object.getCity_alias();
        if (city_alias != null) {
            statement.bindString(3, city_alias);
        } else {
            statement.bindNull(3);
        }
        String city_native = object.getCity_native();
        if (city_native != null) {
            statement.bindString(4, city_native);
        } else {
            statement.bindNull(4);
        }
        String state_name = object.getState_name();
        if (state_name != null) {
            statement.bindString(5, state_name);
        } else {
            statement.bindNull(5);
        }
        String city_code = object.getCity_code();
        if (city_code != null) {
            statement.bindString(6, city_code);
        } else {
            statement.bindNull(6);
        }
        statement.bindLong(7, (long) object.getCity_type());
        String time_zone = object.getTime_zone();
        if (time_zone != null) {
            statement.bindString(8, time_zone);
        } else {
            statement.bindNull(8);
        }
        statement.bindLong(9, object.getInsert_time());
        statement.bindLong(10, object.getWeather_id());
        statement.bindLong(11, (long) object.getManual_set());
        statement.bindLong(12, (long) object.getHome_city());
        String state_name_cn = object.getState_name_cn();
        if (state_name_cn != null) {
            statement.bindString(13, state_name_cn);
        } else {
            statement.bindNull(13);
        }
        String province_name = object.getProvince_name();
        if (province_name != null) {
            statement.bindString(14, province_name);
        } else {
            statement.bindNull(14);
        }
        String province_name_cn = object.getProvince_name_cn();
        if (province_name_cn != null) {
            statement.bindString(15, province_name_cn);
        } else {
            statement.bindNull(15);
        }
        String country_name = object.getCountry_name();
        if (country_name != null) {
            statement.bindString(16, country_name);
        } else {
            statement.bindNull(16);
        }
        String country_name_cn = object.getCountry_name_cn();
        if (country_name_cn != null) {
            statement.bindString(17, country_name_cn);
        } else {
            statement.bindNull(17);
        }
        String hw_id = object.getHw_id();
        if (hw_id != null) {
            statement.bindString(18, hw_id);
        } else {
            statement.bindNull(18);
        }
        String co = object.getCo();
        if (co != null) {
            statement.bindString(19, co);
        } else {
            statement.bindNull(19);
        }
        String ca = object.getCa();
        if (ca != null) {
            statement.bindString(20, ca);
        } else {
            statement.bindNull(20);
        }
        statement.bindLong(21, object.getSequence_id());
    }

    public CityInfoModel readObject(Cursor cursor, int offset) {
        return new CityInfoModel(cursor);
    }

    public void setPrimaryKeyValue(CityInfoModel object, long value) {
        object.set_id(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, CityInfoModel object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
