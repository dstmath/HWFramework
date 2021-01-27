package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZFieldAuthorityHelper extends AEntityHelper<ZFieldAuthority> {
    private static final ZFieldAuthorityHelper INSTANCE = new ZFieldAuthorityHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZFieldAuthority zFieldAuthority) {
        return null;
    }

    private ZFieldAuthorityHelper() {
    }

    public static ZFieldAuthorityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZFieldAuthority zFieldAuthority) {
        Long id = zFieldAuthority.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long tableId = zFieldAuthority.getTableId();
        if (tableId != null) {
            statement.bindLong(2, tableId.longValue());
        } else {
            statement.bindNull(2);
        }
        String tableName = zFieldAuthority.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        Long filedId = zFieldAuthority.getFiledId();
        if (filedId != null) {
            statement.bindLong(4, filedId.longValue());
        } else {
            statement.bindNull(4);
        }
        String fieldName = zFieldAuthority.getFieldName();
        if (fieldName != null) {
            statement.bindString(5, fieldName);
        } else {
            statement.bindNull(5);
        }
        Long packageUid = zFieldAuthority.getPackageUid();
        if (packageUid != null) {
            statement.bindLong(6, packageUid.longValue());
        } else {
            statement.bindNull(6);
        }
        String packageName = zFieldAuthority.getPackageName();
        if (packageName != null) {
            statement.bindString(7, packageName);
        } else {
            statement.bindNull(7);
        }
        Integer authority = zFieldAuthority.getAuthority();
        if (authority != null) {
            statement.bindLong(8, (long) authority.intValue());
        } else {
            statement.bindNull(8);
        }
        String sysAuthorityName = zFieldAuthority.getSysAuthorityName();
        if (sysAuthorityName != null) {
            statement.bindString(9, sysAuthorityName);
        } else {
            statement.bindNull(9);
        }
        Boolean supportGroupAuthority = zFieldAuthority.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(10, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(10);
        }
        String reserved = zFieldAuthority.getReserved();
        if (reserved != null) {
            statement.bindString(11, reserved);
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZFieldAuthority readObject(Cursor cursor, int i) {
        return new ZFieldAuthority(cursor);
    }

    public void setPrimaryKeyValue(ZFieldAuthority zFieldAuthority, long j) {
        zFieldAuthority.setId(Long.valueOf(j));
    }
}
