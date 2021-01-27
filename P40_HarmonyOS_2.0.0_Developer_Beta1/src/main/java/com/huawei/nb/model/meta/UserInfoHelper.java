package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class UserInfoHelper extends AEntityHelper<UserInfo> {
    private static final UserInfoHelper INSTANCE = new UserInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, UserInfo userInfo) {
        return null;
    }

    private UserInfoHelper() {
    }

    public static UserInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, UserInfo userInfo) {
        Integer id = userInfo.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer userId = userInfo.getUserId();
        if (userId != null) {
            statement.bindLong(2, (long) userId.intValue());
        } else {
            statement.bindNull(2);
        }
        Long userSn = userInfo.getUserSn();
        if (userSn != null) {
            statement.bindLong(3, userSn.longValue());
        } else {
            statement.bindNull(3);
        }
        Integer reservedInt1 = userInfo.getReservedInt1();
        if (reservedInt1 != null) {
            statement.bindLong(4, (long) reservedInt1.intValue());
        } else {
            statement.bindNull(4);
        }
        Integer reservedInt2 = userInfo.getReservedInt2();
        if (reservedInt2 != null) {
            statement.bindLong(5, (long) reservedInt2.intValue());
        } else {
            statement.bindNull(5);
        }
        String reservedStr1 = userInfo.getReservedStr1();
        if (reservedStr1 != null) {
            statement.bindString(6, reservedStr1);
        } else {
            statement.bindNull(6);
        }
        String reservedStr2 = userInfo.getReservedStr2();
        if (reservedStr2 != null) {
            statement.bindString(7, reservedStr2);
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public UserInfo readObject(Cursor cursor, int i) {
        return new UserInfo(cursor);
    }

    public void setPrimaryKeyValue(UserInfo userInfo, long j) {
        userInfo.setId(Integer.valueOf((int) j));
    }
}
