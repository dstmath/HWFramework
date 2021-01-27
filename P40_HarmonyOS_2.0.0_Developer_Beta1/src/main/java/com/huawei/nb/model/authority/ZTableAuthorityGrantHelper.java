package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZTableAuthorityGrantHelper extends AEntityHelper<ZTableAuthorityGrant> {
    private static final ZTableAuthorityGrantHelper INSTANCE = new ZTableAuthorityGrantHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZTableAuthorityGrant zTableAuthorityGrant) {
        return null;
    }

    private ZTableAuthorityGrantHelper() {
    }

    public static ZTableAuthorityGrantHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZTableAuthorityGrant zTableAuthorityGrant) {
        Long id = zTableAuthorityGrant.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long tableId = zTableAuthorityGrant.getTableId();
        if (tableId != null) {
            statement.bindLong(2, tableId.longValue());
        } else {
            statement.bindNull(2);
        }
        String tableName = zTableAuthorityGrant.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        Long packageUid = zTableAuthorityGrant.getPackageUid();
        if (packageUid != null) {
            statement.bindLong(4, packageUid.longValue());
        } else {
            statement.bindNull(4);
        }
        String packageName = zTableAuthorityGrant.getPackageName();
        if (packageName != null) {
            statement.bindString(5, packageName);
        } else {
            statement.bindNull(5);
        }
        Integer authority = zTableAuthorityGrant.getAuthority();
        if (authority != null) {
            statement.bindLong(6, (long) authority.intValue());
        } else {
            statement.bindNull(6);
        }
        Boolean supportGroupAuthority = zTableAuthorityGrant.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(7, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(7);
        }
        String reserved = zTableAuthorityGrant.getReserved();
        if (reserved != null) {
            statement.bindString(8, reserved);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZTableAuthorityGrant readObject(Cursor cursor, int i) {
        return new ZTableAuthorityGrant(cursor);
    }

    public void setPrimaryKeyValue(ZTableAuthorityGrant zTableAuthorityGrant, long j) {
        zTableAuthorityGrant.setId(Long.valueOf(j));
    }
}
