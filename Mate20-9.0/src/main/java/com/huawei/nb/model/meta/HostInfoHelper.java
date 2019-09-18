package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class HostInfoHelper extends AEntityHelper<HostInfo> {
    private static final HostInfoHelper INSTANCE = new HostInfoHelper();

    private HostInfoHelper() {
    }

    public static HostInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, HostInfo object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String dbName = object.getDbName();
        if (dbName != null) {
            statement.bindString(2, dbName);
        } else {
            statement.bindNull(2);
        }
        String tableName = object.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        String recordName = object.getRecordName();
        if (recordName != null) {
            statement.bindString(4, recordName);
        } else {
            statement.bindNull(4);
        }
    }

    public HostInfo readObject(Cursor cursor, int offset) {
        return new HostInfo(cursor);
    }

    public void setPrimaryKeyValue(HostInfo object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, HostInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
