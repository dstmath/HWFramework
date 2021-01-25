package com.huawei.nb.model.weather;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CityInfoModelHelper extends AEntityHelper<CityInfoModel> {
    private static final CityInfoModelHelper INSTANCE = new CityInfoModelHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CityInfoModel cityInfoModel) {
        return null;
    }

    private CityInfoModelHelper() {
    }

    public static CityInfoModelHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CityInfoModel cityInfoModel) {
        Long l = cityInfoModel.get_id();
        if (l != null) {
            statement.bindLong(1, l.longValue());
        } else {
            statement.bindNull(1);
        }
        String city_name = cityInfoModel.getCity_name();
        if (city_name != null) {
            statement.bindString(2, city_name);
        } else {
            statement.bindNull(2);
        }
        String city_alias = cityInfoModel.getCity_alias();
        if (city_alias != null) {
            statement.bindString(3, city_alias);
        } else {
            statement.bindNull(3);
        }
        String city_native = cityInfoModel.getCity_native();
        if (city_native != null) {
            statement.bindString(4, city_native);
        } else {
            statement.bindNull(4);
        }
        String state_name = cityInfoModel.getState_name();
        if (state_name != null) {
            statement.bindString(5, state_name);
        } else {
            statement.bindNull(5);
        }
        String city_code = cityInfoModel.getCity_code();
        if (city_code != null) {
            statement.bindString(6, city_code);
        } else {
            statement.bindNull(6);
        }
        statement.bindLong(7, (long) cityInfoModel.getCity_type());
        String time_zone = cityInfoModel.getTime_zone();
        if (time_zone != null) {
            statement.bindString(8, time_zone);
        } else {
            statement.bindNull(8);
        }
        statement.bindLong(9, cityInfoModel.getInsert_time());
        statement.bindLong(10, cityInfoModel.getWeather_id());
        statement.bindLong(11, (long) cityInfoModel.getManual_set());
        statement.bindLong(12, (long) cityInfoModel.getHome_city());
        String state_name_cn = cityInfoModel.getState_name_cn();
        if (state_name_cn != null) {
            statement.bindString(13, state_name_cn);
        } else {
            statement.bindNull(13);
        }
        String province_name = cityInfoModel.getProvince_name();
        if (province_name != null) {
            statement.bindString(14, province_name);
        } else {
            statement.bindNull(14);
        }
        String province_name_cn = cityInfoModel.getProvince_name_cn();
        if (province_name_cn != null) {
            statement.bindString(15, province_name_cn);
        } else {
            statement.bindNull(15);
        }
        String country_name = cityInfoModel.getCountry_name();
        if (country_name != null) {
            statement.bindString(16, country_name);
        } else {
            statement.bindNull(16);
        }
        String country_name_cn = cityInfoModel.getCountry_name_cn();
        if (country_name_cn != null) {
            statement.bindString(17, country_name_cn);
        } else {
            statement.bindNull(17);
        }
        String hw_id = cityInfoModel.getHw_id();
        if (hw_id != null) {
            statement.bindString(18, hw_id);
        } else {
            statement.bindNull(18);
        }
        String co = cityInfoModel.getCo();
        if (co != null) {
            statement.bindString(19, co);
        } else {
            statement.bindNull(19);
        }
        String ca = cityInfoModel.getCa();
        if (ca != null) {
            statement.bindString(20, ca);
        } else {
            statement.bindNull(20);
        }
        statement.bindLong(21, cityInfoModel.getSequence_id());
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CityInfoModel readObject(Cursor cursor, int i) {
        return new CityInfoModel(cursor);
    }

    public void setPrimaryKeyValue(CityInfoModel cityInfoModel, long j) {
        cityInfoModel.set_id(Long.valueOf(j));
    }
}
