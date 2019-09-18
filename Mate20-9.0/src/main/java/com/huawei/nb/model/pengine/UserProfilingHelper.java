package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class UserProfilingHelper extends AEntityHelper<UserProfiling> {
    private static final UserProfilingHelper INSTANCE = new UserProfilingHelper();

    private UserProfilingHelper() {
    }

    public static UserProfilingHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, UserProfiling object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String parent = object.getParent();
        if (parent != null) {
            statement.bindString(2, parent);
        } else {
            statement.bindNull(2);
        }
        Integer level = object.getLevel();
        if (level != null) {
            statement.bindLong(3, (long) level.intValue());
        } else {
            statement.bindNull(3);
        }
        String uriKey = object.getUriKey();
        if (uriKey != null) {
            statement.bindString(4, uriKey);
        } else {
            statement.bindNull(4);
        }
        String uriValue = object.getUriValue();
        if (uriValue != null) {
            statement.bindString(5, uriValue);
        } else {
            statement.bindNull(5);
        }
        Long timestamp = object.getTimestamp();
        if (timestamp != null) {
            statement.bindLong(6, timestamp.longValue());
        } else {
            statement.bindNull(6);
        }
    }

    public UserProfiling readObject(Cursor cursor, int offset) {
        return new UserProfiling(cursor);
    }

    public void setPrimaryKeyValue(UserProfiling object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, UserProfiling object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
