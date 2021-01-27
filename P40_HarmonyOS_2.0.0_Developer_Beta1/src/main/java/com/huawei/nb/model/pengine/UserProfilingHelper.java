package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class UserProfilingHelper extends AEntityHelper<UserProfiling> {
    private static final UserProfilingHelper INSTANCE = new UserProfilingHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, UserProfiling userProfiling) {
        return null;
    }

    private UserProfilingHelper() {
    }

    public static UserProfilingHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, UserProfiling userProfiling) {
        Integer id = userProfiling.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String parent = userProfiling.getParent();
        if (parent != null) {
            statement.bindString(2, parent);
        } else {
            statement.bindNull(2);
        }
        Integer level = userProfiling.getLevel();
        if (level != null) {
            statement.bindLong(3, (long) level.intValue());
        } else {
            statement.bindNull(3);
        }
        String uriKey = userProfiling.getUriKey();
        if (uriKey != null) {
            statement.bindString(4, uriKey);
        } else {
            statement.bindNull(4);
        }
        String uriValue = userProfiling.getUriValue();
        if (uriValue != null) {
            statement.bindString(5, uriValue);
        } else {
            statement.bindNull(5);
        }
        Long timestamp = userProfiling.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(6, timestamp.longValue());
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public UserProfiling readObject(Cursor cursor, int i) {
        return new UserProfiling(cursor);
    }

    public void setPrimaryKeyValue(UserProfiling userProfiling, long j) {
        userProfiling.setId(Integer.valueOf((int) j));
    }
}
