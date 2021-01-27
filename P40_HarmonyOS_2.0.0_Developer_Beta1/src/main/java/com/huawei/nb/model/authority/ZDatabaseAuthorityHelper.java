package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZDatabaseAuthorityHelper extends AEntityHelper<ZDatabaseAuthority> {
    private static final ZDatabaseAuthorityHelper INSTANCE = new ZDatabaseAuthorityHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZDatabaseAuthority zDatabaseAuthority) {
        return null;
    }

    private ZDatabaseAuthorityHelper() {
    }

    public static ZDatabaseAuthorityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZDatabaseAuthority zDatabaseAuthority) {
        Long id = zDatabaseAuthority.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long dbId = zDatabaseAuthority.getDbId();
        if (dbId != null) {
            statement.bindLong(2, dbId.longValue());
        } else {
            statement.bindNull(2);
        }
        String dbName = zDatabaseAuthority.getDbName();
        if (dbName != null) {
            statement.bindString(3, dbName);
        } else {
            statement.bindNull(3);
        }
        Long packageUid = zDatabaseAuthority.getPackageUid();
        if (packageUid != null) {
            statement.bindLong(4, packageUid.longValue());
        } else {
            statement.bindNull(4);
        }
        String packageName = zDatabaseAuthority.getPackageName();
        if (packageName != null) {
            statement.bindString(5, packageName);
        } else {
            statement.bindNull(5);
        }
        Integer authority = zDatabaseAuthority.getAuthority();
        if (authority != null) {
            statement.bindLong(6, (long) authority.intValue());
        } else {
            statement.bindNull(6);
        }
        Boolean supportGroupAuthority = zDatabaseAuthority.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(7, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(7);
        }
        String reserved = zDatabaseAuthority.getReserved();
        if (reserved != null) {
            statement.bindString(8, reserved);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZDatabaseAuthority readObject(Cursor cursor, int i) {
        return new ZDatabaseAuthority(cursor);
    }

    public void setPrimaryKeyValue(ZDatabaseAuthority zDatabaseAuthority, long j) {
        zDatabaseAuthority.setId(Long.valueOf(j));
    }
}
