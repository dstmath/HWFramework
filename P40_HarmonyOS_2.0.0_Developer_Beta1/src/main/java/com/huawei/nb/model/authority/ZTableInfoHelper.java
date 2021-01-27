package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZTableInfoHelper extends AEntityHelper<ZTableInfo> {
    private static final ZTableInfoHelper INSTANCE = new ZTableInfoHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZTableInfo zTableInfo) {
        return null;
    }

    private ZTableInfoHelper() {
    }

    public static ZTableInfoHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZTableInfo zTableInfo) {
        Long id = zTableInfo.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long tableId = zTableInfo.getTableId();
        if (tableId != null) {
            statement.bindLong(2, tableId.longValue());
        } else {
            statement.bindNull(2);
        }
        String tableName = zTableInfo.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        String tableDesc = zTableInfo.getTableDesc();
        if (tableDesc != null) {
            statement.bindString(4, tableDesc);
        } else {
            statement.bindNull(4);
        }
        Integer authorityLevel = zTableInfo.getAuthorityLevel();
        if (authorityLevel != null) {
            statement.bindLong(5, (long) authorityLevel.intValue());
        } else {
            statement.bindNull(5);
        }
        Integer authorityValue = zTableInfo.getAuthorityValue();
        if (authorityValue != null) {
            statement.bindLong(6, (long) authorityValue.intValue());
        } else {
            statement.bindNull(6);
        }
        String reserved = zTableInfo.getReserved();
        if (reserved != null) {
            statement.bindString(7, reserved);
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZTableInfo readObject(Cursor cursor, int i) {
        return new ZTableInfo(cursor);
    }

    public void setPrimaryKeyValue(ZTableInfo zTableInfo, long j) {
        zTableInfo.setId(Long.valueOf(j));
    }
}
