package com.huawei.nb.model.geofence;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class MySpaceHelper extends AEntityHelper<MySpace> {
    private static final MySpaceHelper INSTANCE = new MySpaceHelper();

    private MySpaceHelper() {
    }

    public static MySpaceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MySpace object) {
        Long mID = object.getMID();
        if (mID != null) {
            statement.bindLong(1, mID.longValue());
        } else {
            statement.bindNull(1);
        }
        String home = object.getHome();
        if (home != null) {
            statement.bindString(2, home);
        } else {
            statement.bindNull(2);
        }
        String work = object.getWork();
        if (work != null) {
            statement.bindString(3, work);
        } else {
            statement.bindNull(3);
        }
        String aoi1 = object.getAoi1();
        if (aoi1 != null) {
            statement.bindString(4, aoi1);
        } else {
            statement.bindNull(4);
        }
        String aoi2 = object.getAoi2();
        if (aoi2 != null) {
            statement.bindString(5, aoi2);
        } else {
            statement.bindNull(5);
        }
        String aoi3 = object.getAoi3();
        if (aoi3 != null) {
            statement.bindString(6, aoi3);
        } else {
            statement.bindNull(6);
        }
        String aoi4 = object.getAoi4();
        if (aoi4 != null) {
            statement.bindString(7, aoi4);
        } else {
            statement.bindNull(7);
        }
        String aoi5 = object.getAoi5();
        if (aoi5 != null) {
            statement.bindString(8, aoi5);
        } else {
            statement.bindNull(8);
        }
        Float homeConfi = object.getHomeConfi();
        if (homeConfi != null) {
            statement.bindString(9, Float.toString(homeConfi.floatValue()));
        } else {
            statement.bindNull(9);
        }
        Float workConfi = object.getWorkConfi();
        if (workConfi != null) {
            statement.bindString(10, Float.toString(workConfi.floatValue()));
        } else {
            statement.bindNull(10);
        }
        Float aoi1Confi = object.getAoi1Confi();
        if (aoi1Confi != null) {
            statement.bindString(11, Float.toString(aoi1Confi.floatValue()));
        } else {
            statement.bindNull(11);
        }
        Float aoi2Confi = object.getAoi2Confi();
        if (aoi2Confi != null) {
            statement.bindString(12, Float.toString(aoi2Confi.floatValue()));
        } else {
            statement.bindNull(12);
        }
        Float aoi3Confi = object.getAoi3Confi();
        if (aoi3Confi != null) {
            statement.bindString(13, Float.toString(aoi3Confi.floatValue()));
        } else {
            statement.bindNull(13);
        }
        Float aoi4Confi = object.getAoi4Confi();
        if (aoi4Confi != null) {
            statement.bindString(14, Float.toString(aoi4Confi.floatValue()));
        } else {
            statement.bindNull(14);
        }
        Float aoi5Confi = object.getAoi5Confi();
        if (aoi5Confi != null) {
            statement.bindString(15, Float.toString(aoi5Confi.floatValue()));
        } else {
            statement.bindNull(15);
        }
        String meal = object.getMeal();
        if (meal != null) {
            statement.bindString(16, meal);
        } else {
            statement.bindNull(16);
        }
        String fun = object.getFun();
        if (fun != null) {
            statement.bindString(17, fun);
        } else {
            statement.bindNull(17);
        }
        String childrenSchool = object.getChildrenSchool();
        if (childrenSchool != null) {
            statement.bindString(18, childrenSchool);
        } else {
            statement.bindNull(18);
        }
        String friends = object.getFriends();
        if (friends != null) {
            statement.bindString(19, friends);
        } else {
            statement.bindNull(19);
        }
        String gym = object.getGym();
        if (gym != null) {
            statement.bindString(20, gym);
        } else {
            statement.bindNull(20);
        }
        String allAOI = object.getAllAOI();
        if (allAOI != null) {
            statement.bindString(21, allAOI);
        } else {
            statement.bindNull(21);
        }
        String familiarCities = object.getFamiliarCities();
        if (familiarCities != null) {
            statement.bindString(22, familiarCities);
        } else {
            statement.bindNull(22);
        }
        String homeCity = object.getHomeCity();
        if (homeCity != null) {
            statement.bindString(23, homeCity);
        } else {
            statement.bindNull(23);
        }
        Float homeCityConfi = object.getHomeCityConfi();
        if (homeCityConfi != null) {
            statement.bindString(24, Float.toString(homeCityConfi.floatValue()));
        } else {
            statement.bindNull(24);
        }
        String workCity = object.getWorkCity();
        if (workCity != null) {
            statement.bindString(25, workCity);
        } else {
            statement.bindNull(25);
        }
        Float workCityConfi = object.getWorkCityConfi();
        if (workCityConfi != null) {
            statement.bindString(26, Float.toString(workCityConfi.floatValue()));
        } else {
            statement.bindNull(26);
        }
        String birthCity = object.getBirthCity();
        if (birthCity != null) {
            statement.bindString(27, birthCity);
        } else {
            statement.bindNull(27);
        }
        String currentCity = object.getCurrentCity();
        if (currentCity != null) {
            statement.bindString(28, currentCity);
        } else {
            statement.bindNull(28);
        }
    }

    public MySpace readObject(Cursor cursor, int offset) {
        return new MySpace(cursor);
    }

    public void setPrimaryKeyValue(MySpace object, long value) {
        object.setMID(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, MySpace object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
