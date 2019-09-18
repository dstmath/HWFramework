package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZFieldAuthorityHelper extends AEntityHelper<ZFieldAuthority> {
    private static final ZFieldAuthorityHelper INSTANCE = new ZFieldAuthorityHelper();

    private ZFieldAuthorityHelper() {
    }

    public static ZFieldAuthorityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZFieldAuthority object) {
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
        Long filedId = object.getFiledId();
        if (filedId != null) {
            statement.bindLong(4, filedId.longValue());
        } else {
            statement.bindNull(4);
        }
        String fieldName = object.getFieldName();
        if (fieldName != null) {
            statement.bindString(5, fieldName);
        } else {
            statement.bindNull(5);
        }
        Long packageUid = object.getPackageUid();
        if (packageUid != null) {
            statement.bindLong(6, packageUid.longValue());
        } else {
            statement.bindNull(6);
        }
        String packageName = object.getPackageName();
        if (packageName != null) {
            statement.bindString(7, packageName);
        } else {
            statement.bindNull(7);
        }
        Integer authority = object.getAuthority();
        if (authority != null) {
            statement.bindLong(8, (long) authority.intValue());
        } else {
            statement.bindNull(8);
        }
        String sysAuthorityName = object.getSysAuthorityName();
        if (sysAuthorityName != null) {
            statement.bindString(9, sysAuthorityName);
        } else {
            statement.bindNull(9);
        }
        Boolean supportGroupAuthority = object.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(10, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(10);
        }
        String reserved = object.getReserved();
        if (reserved != null) {
            statement.bindString(11, reserved);
        } else {
            statement.bindNull(11);
        }
    }

    public ZFieldAuthority readObject(Cursor cursor, int offset) {
        return new ZFieldAuthority(cursor);
    }

    public void setPrimaryKeyValue(ZFieldAuthority object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ZFieldAuthority object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
