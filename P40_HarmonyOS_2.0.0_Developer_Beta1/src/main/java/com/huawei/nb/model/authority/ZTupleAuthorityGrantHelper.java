package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZTupleAuthorityGrantHelper extends AEntityHelper<ZTupleAuthorityGrant> {
    private static final ZTupleAuthorityGrantHelper INSTANCE = new ZTupleAuthorityGrantHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZTupleAuthorityGrant zTupleAuthorityGrant) {
        return null;
    }

    private ZTupleAuthorityGrantHelper() {
    }

    public static ZTupleAuthorityGrantHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZTupleAuthorityGrant zTupleAuthorityGrant) {
        Long id = zTupleAuthorityGrant.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long tableId = zTupleAuthorityGrant.getTableId();
        if (tableId != null) {
            statement.bindLong(2, tableId.longValue());
        } else {
            statement.bindNull(2);
        }
        String tableName = zTupleAuthorityGrant.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        Long tupleId = zTupleAuthorityGrant.getTupleId();
        if (tupleId != null) {
            statement.bindLong(4, tupleId.longValue());
        } else {
            statement.bindNull(4);
        }
        String tupleName = zTupleAuthorityGrant.getTupleName();
        if (tupleName != null) {
            statement.bindString(5, tupleName);
        } else {
            statement.bindNull(5);
        }
        Long packageUid = zTupleAuthorityGrant.getPackageUid();
        if (packageUid != null) {
            statement.bindLong(6, packageUid.longValue());
        } else {
            statement.bindNull(6);
        }
        String packageName = zTupleAuthorityGrant.getPackageName();
        if (packageName != null) {
            statement.bindString(7, packageName);
        } else {
            statement.bindNull(7);
        }
        Integer authority = zTupleAuthorityGrant.getAuthority();
        if (authority != null) {
            statement.bindLong(8, (long) authority.intValue());
        } else {
            statement.bindNull(8);
        }
        Boolean supportGroupAuthority = zTupleAuthorityGrant.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(9, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(9);
        }
        String reserved = zTupleAuthorityGrant.getReserved();
        if (reserved != null) {
            statement.bindString(10, reserved);
        } else {
            statement.bindNull(10);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZTupleAuthorityGrant readObject(Cursor cursor, int i) {
        return new ZTupleAuthorityGrant(cursor);
    }

    public void setPrimaryKeyValue(ZTupleAuthorityGrant zTupleAuthorityGrant, long j) {
        zTupleAuthorityGrant.setId(Long.valueOf(j));
    }
}
