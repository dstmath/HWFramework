package com.huawei.nb.model.profile;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class UserProfileHelper extends AEntityHelper<UserProfile> {
    private static final UserProfileHelper INSTANCE = new UserProfileHelper();

    private UserProfileHelper() {
    }

    public static UserProfileHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, UserProfile object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String regDate = object.getRegDate();
        if (regDate != null) {
            statement.bindString(2, regDate);
        } else {
            statement.bindNull(2);
        }
        String deviceToken = object.getDeviceToken();
        if (deviceToken != null) {
            statement.bindString(3, deviceToken);
        } else {
            statement.bindNull(3);
        }
        String deviceID = object.getDeviceID();
        if (deviceID != null) {
            statement.bindString(4, deviceID);
        } else {
            statement.bindNull(4);
        }
        String hwId = object.getHwId();
        if (hwId != null) {
            statement.bindString(5, hwId);
        } else {
            statement.bindNull(5);
        }
        String userProfile = object.getUserProfile();
        if (userProfile != null) {
            statement.bindString(6, userProfile);
        } else {
            statement.bindNull(6);
        }
    }

    public UserProfile readObject(Cursor cursor, int offset) {
        return new UserProfile(cursor);
    }

    public void setPrimaryKeyValue(UserProfile object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, UserProfile object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
