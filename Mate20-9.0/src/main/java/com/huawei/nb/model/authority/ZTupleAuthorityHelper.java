package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZTupleAuthorityHelper extends AEntityHelper<ZTupleAuthority> {
    private static final ZTupleAuthorityHelper INSTANCE = new ZTupleAuthorityHelper();

    private ZTupleAuthorityHelper() {
    }

    public static ZTupleAuthorityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZTupleAuthority object) {
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
        Long tupleId = object.getTupleId();
        if (tupleId != null) {
            statement.bindLong(4, tupleId.longValue());
        } else {
            statement.bindNull(4);
        }
        String tupleName = object.getTupleName();
        if (tupleName != null) {
            statement.bindString(5, tupleName);
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
        Boolean supportGroupAuthority = object.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(9, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(9);
        }
        String reserved = object.getReserved();
        if (reserved != null) {
            statement.bindString(10, reserved);
        } else {
            statement.bindNull(10);
        }
    }

    public ZTupleAuthority readObject(Cursor cursor, int offset) {
        return new ZTupleAuthority(cursor);
    }

    public void setPrimaryKeyValue(ZTupleAuthority object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ZTupleAuthority object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
