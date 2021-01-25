package com.huawei.nb.model.geofence;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class MySpaceHelper extends AEntityHelper<MySpace> {
    private static final MySpaceHelper INSTANCE = new MySpaceHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, MySpace mySpace) {
        return null;
    }

    private MySpaceHelper() {
    }

    public static MySpaceHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, MySpace mySpace) {
        Long mid = mySpace.getMID();
        if (mid != null) {
            statement.bindLong(1, mid.longValue());
        } else {
            statement.bindNull(1);
        }
        String home = mySpace.getHome();
        if (home != null) {
            statement.bindString(2, home);
        } else {
            statement.bindNull(2);
        }
        String work = mySpace.getWork();
        if (work != null) {
            statement.bindString(3, work);
        } else {
            statement.bindNull(3);
        }
        String aoi1 = mySpace.getAoi1();
        if (aoi1 != null) {
            statement.bindString(4, aoi1);
        } else {
            statement.bindNull(4);
        }
        String aoi2 = mySpace.getAoi2();
        if (aoi2 != null) {
            statement.bindString(5, aoi2);
        } else {
            statement.bindNull(5);
        }
        String aoi3 = mySpace.getAoi3();
        if (aoi3 != null) {
            statement.bindString(6, aoi3);
        } else {
            statement.bindNull(6);
        }
        String aoi4 = mySpace.getAoi4();
        if (aoi4 != null) {
            statement.bindString(7, aoi4);
        } else {
            statement.bindNull(7);
        }
        String aoi5 = mySpace.getAoi5();
        if (aoi5 != null) {
            statement.bindString(8, aoi5);
        } else {
            statement.bindNull(8);
        }
        Float homeConfi = mySpace.getHomeConfi();
        if (homeConfi != null) {
            statement.bindString(9, Float.toString(homeConfi.floatValue()));
        } else {
            statement.bindNull(9);
        }
        Float workConfi = mySpace.getWorkConfi();
        if (workConfi != null) {
            statement.bindString(10, Float.toString(workConfi.floatValue()));
        } else {
            statement.bindNull(10);
        }
        Float aoi1Confi = mySpace.getAoi1Confi();
        if (aoi1Confi != null) {
            statement.bindString(11, Float.toString(aoi1Confi.floatValue()));
        } else {
            statement.bindNull(11);
        }
        Float aoi2Confi = mySpace.getAoi2Confi();
        if (aoi2Confi != null) {
            statement.bindString(12, Float.toString(aoi2Confi.floatValue()));
        } else {
            statement.bindNull(12);
        }
        Float aoi3Confi = mySpace.getAoi3Confi();
        if (aoi3Confi != null) {
            statement.bindString(13, Float.toString(aoi3Confi.floatValue()));
        } else {
            statement.bindNull(13);
        }
        Float aoi4Confi = mySpace.getAoi4Confi();
        if (aoi4Confi != null) {
            statement.bindString(14, Float.toString(aoi4Confi.floatValue()));
        } else {
            statement.bindNull(14);
        }
        Float aoi5Confi = mySpace.getAoi5Confi();
        if (aoi5Confi != null) {
            statement.bindString(15, Float.toString(aoi5Confi.floatValue()));
        } else {
            statement.bindNull(15);
        }
        String meal = mySpace.getMeal();
        if (meal != null) {
            statement.bindString(16, meal);
        } else {
            statement.bindNull(16);
        }
        String fun = mySpace.getFun();
        if (fun != null) {
            statement.bindString(17, fun);
        } else {
            statement.bindNull(17);
        }
        String childrenSchool = mySpace.getChildrenSchool();
        if (childrenSchool != null) {
            statement.bindString(18, childrenSchool);
        } else {
            statement.bindNull(18);
        }
        String friends = mySpace.getFriends();
        if (friends != null) {
            statement.bindString(19, friends);
        } else {
            statement.bindNull(19);
        }
        String gym = mySpace.getGym();
        if (gym != null) {
            statement.bindString(20, gym);
        } else {
            statement.bindNull(20);
        }
        String allAOI = mySpace.getAllAOI();
        if (allAOI != null) {
            statement.bindString(21, allAOI);
        } else {
            statement.bindNull(21);
        }
        String familiarCities = mySpace.getFamiliarCities();
        if (familiarCities != null) {
            statement.bindString(22, familiarCities);
        } else {
            statement.bindNull(22);
        }
        String homeCity = mySpace.getHomeCity();
        if (homeCity != null) {
            statement.bindString(23, homeCity);
        } else {
            statement.bindNull(23);
        }
        Float homeCityConfi = mySpace.getHomeCityConfi();
        if (homeCityConfi != null) {
            statement.bindString(24, Float.toString(homeCityConfi.floatValue()));
        } else {
            statement.bindNull(24);
        }
        String workCity = mySpace.getWorkCity();
        if (workCity != null) {
            statement.bindString(25, workCity);
        } else {
            statement.bindNull(25);
        }
        Float workCityConfi = mySpace.getWorkCityConfi();
        if (workCityConfi != null) {
            statement.bindString(26, Float.toString(workCityConfi.floatValue()));
        } else {
            statement.bindNull(26);
        }
        String birthCity = mySpace.getBirthCity();
        if (birthCity != null) {
            statement.bindString(27, birthCity);
        } else {
            statement.bindNull(27);
        }
        String currentCity = mySpace.getCurrentCity();
        if (currentCity != null) {
            statement.bindString(28, currentCity);
        } else {
            statement.bindNull(28);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public MySpace readObject(Cursor cursor, int i) {
        return new MySpace(cursor);
    }

    public void setPrimaryKeyValue(MySpace mySpace, long j) {
        mySpace.setMID(Long.valueOf(j));
    }
}
