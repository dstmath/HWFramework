package com.huawei.nb.model.profile;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class UserProfileHelper extends AEntityHelper<UserProfile> {
    private static final UserProfileHelper INSTANCE = new UserProfileHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, UserProfile userProfile) {
        return null;
    }

    private UserProfileHelper() {
    }

    public static UserProfileHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, UserProfile userProfile) {
        Integer id = userProfile.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String regDate = userProfile.getRegDate();
        if (regDate != null) {
            statement.bindString(2, regDate);
        } else {
            statement.bindNull(2);
        }
        String deviceToken = userProfile.getDeviceToken();
        if (deviceToken != null) {
            statement.bindString(3, deviceToken);
        } else {
            statement.bindNull(3);
        }
        String deviceID = userProfile.getDeviceID();
        if (deviceID != null) {
            statement.bindString(4, deviceID);
        } else {
            statement.bindNull(4);
        }
        String hwId = userProfile.getHwId();
        if (hwId != null) {
            statement.bindString(5, hwId);
        } else {
            statement.bindNull(5);
        }
        String userProfile2 = userProfile.getUserProfile();
        if (userProfile2 != null) {
            statement.bindString(6, userProfile2);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public UserProfile readObject(Cursor cursor, int i) {
        return new UserProfile(cursor);
    }

    public void setPrimaryKeyValue(UserProfile userProfile, long j) {
        userProfile.setId(Integer.valueOf((int) j));
    }
}
