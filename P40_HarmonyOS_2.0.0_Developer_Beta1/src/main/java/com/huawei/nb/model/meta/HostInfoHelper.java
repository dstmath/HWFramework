package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class HostInfoHelper extends AEntityHelper<HostInfo> {
    private static final HostInfoHelper INSTANCE = new HostInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, HostInfo hostInfo) {
        return null;
    }

    private HostInfoHelper() {
    }

    public static HostInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, HostInfo hostInfo) {
        Integer id = hostInfo.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String dbName = hostInfo.getDbName();
        if (dbName != null) {
            statement.bindString(2, dbName);
        } else {
            statement.bindNull(2);
        }
        String tableName = hostInfo.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        String recordName = hostInfo.getRecordName();
        if (recordName != null) {
            statement.bindString(4, recordName);
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public HostInfo readObject(Cursor cursor, int i) {
        return new HostInfo(cursor);
    }

    public void setPrimaryKeyValue(HostInfo hostInfo, long j) {
        hostInfo.setId(Integer.valueOf((int) j));
    }
}
