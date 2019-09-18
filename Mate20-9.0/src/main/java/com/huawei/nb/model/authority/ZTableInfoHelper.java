package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZTableInfoHelper extends AEntityHelper<ZTableInfo> {
    private static final ZTableInfoHelper INSTANCE = new ZTableInfoHelper();

    private ZTableInfoHelper() {
    }

    public static ZTableInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZTableInfo object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long tableId = object.getTableId();
        if (tableId != null) {
            statement.bindLong(2, tableId.longValue());
        } else {
            statement.bindNull(2);
        }
        String tableName = object.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        String tableDesc = object.getTableDesc();
        if (tableDesc != null) {
            statement.bindString(4, tableDesc);
        } else {
            statement.bindNull(4);
        }
        Integer authorityLevel = object.getAuthorityLevel();
        if (authorityLevel != null) {
            statement.bindLong(5, (long) authorityLevel.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer authorityValue = object.getAuthorityValue();
        if (authorityValue != null) {
            statement.bindLong(6, (long) authorityValue.intValue());
        } else {
            statement.bindNull(6);
        }
        String reserved = object.getReserved();
        if (reserved != null) {
            statement.bindString(7, reserved);
        } else {
            statement.bindNull(7);
        }
    }

    public ZTableInfo readObject(Cursor cursor, int offset) {
        return new ZTableInfo(cursor);
    }

    public void setPrimaryKeyValue(ZTableInfo object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ZTableInfo object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
