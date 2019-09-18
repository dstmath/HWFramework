package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZTableAuthorityGrantHelper extends AEntityHelper<ZTableAuthorityGrant> {
    private static final ZTableAuthorityGrantHelper INSTANCE = new ZTableAuthorityGrantHelper();

    private ZTableAuthorityGrantHelper() {
    }

    public static ZTableAuthorityGrantHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZTableAuthorityGrant object) {
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
        Long packageUid = object.getPackageUid();
        if (packageUid != null) {
            statement.bindLong(4, packageUid.longValue());
        } else {
            statement.bindNull(4);
        }
        String packageName = object.getPackageName();
        if (packageName != null) {
            statement.bindString(5, packageName);
        } else {
            statement.bindNull(5);
        }
        Integer authority = object.getAuthority();
        if (authority != null) {
            statement.bindLong(6, (long) authority.intValue());
        } else {
            statement.bindNull(6);
        }
        Boolean supportGroupAuthority = object.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(7, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(7);
        }
        String reserved = object.getReserved();
        if (reserved != null) {
            statement.bindString(8, reserved);
        } else {
            statement.bindNull(8);
        }
    }

    public ZTableAuthorityGrant readObject(Cursor cursor, int offset) {
        return new ZTableAuthorityGrant(cursor);
    }

    public void setPrimaryKeyValue(ZTableAuthorityGrant object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ZTableAuthorityGrant object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
