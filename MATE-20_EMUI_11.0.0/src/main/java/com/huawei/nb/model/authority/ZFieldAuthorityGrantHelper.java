package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZFieldAuthorityGrantHelper extends AEntityHelper<ZFieldAuthorityGrant> {
    private static final ZFieldAuthorityGrantHelper INSTANCE = new ZFieldAuthorityGrantHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZFieldAuthorityGrant zFieldAuthorityGrant) {
        return null;
    }

    private ZFieldAuthorityGrantHelper() {
    }

    public static ZFieldAuthorityGrantHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZFieldAuthorityGrant zFieldAuthorityGrant) {
        Long id = zFieldAuthorityGrant.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long tableId = zFieldAuthorityGrant.getTableId();
        if (tableId != null) {
            statement.bindLong(2, tableId.longValue());
        } else {
            statement.bindNull(2);
        }
        String tableName = zFieldAuthorityGrant.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        Long filedId = zFieldAuthorityGrant.getFiledId();
        if (filedId != null) {
            statement.bindLong(4, filedId.longValue());
        } else {
            statement.bindNull(4);
        }
        String fieldName = zFieldAuthorityGrant.getFieldName();
        if (fieldName != null) {
            statement.bindString(5, fieldName);
        } else {
            statement.bindNull(5);
        }
        Long packageUid = zFieldAuthorityGrant.getPackageUid();
        if (packageUid != null) {
            statement.bindLong(6, packageUid.longValue());
        } else {
            statement.bindNull(6);
        }
        String packageName = zFieldAuthorityGrant.getPackageName();
        if (packageName != null) {
            statement.bindString(7, packageName);
        } else {
            statement.bindNull(7);
        }
        Integer authority = zFieldAuthorityGrant.getAuthority();
        if (authority != null) {
            statement.bindLong(8, (long) authority.intValue());
        } else {
            statement.bindNull(8);
        }
        String sysAuthorityName = zFieldAuthorityGrant.getSysAuthorityName();
        if (sysAuthorityName != null) {
            statement.bindString(9, sysAuthorityName);
        } else {
            statement.bindNull(9);
        }
        Boolean supportGroupAuthority = zFieldAuthorityGrant.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(10, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(10);
        }
        String reserved = zFieldAuthorityGrant.getReserved();
        if (reserved != null) {
            statement.bindString(11, reserved);
        } else {
            statement.bindNull(11);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZFieldAuthorityGrant readObject(Cursor cursor, int i) {
        return new ZFieldAuthorityGrant(cursor);
    }

    public void setPrimaryKeyValue(ZFieldAuthorityGrant zFieldAuthorityGrant, long j) {
        zFieldAuthorityGrant.setId(Long.valueOf(j));
    }
}
