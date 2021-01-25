package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZTableAuthorityHelper extends AEntityHelper<ZTableAuthority> {
    private static final ZTableAuthorityHelper INSTANCE = new ZTableAuthorityHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZTableAuthority zTableAuthority) {
        return null;
    }

    private ZTableAuthorityHelper() {
    }

    public static ZTableAuthorityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZTableAuthority zTableAuthority) {
        Long id = zTableAuthority.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long tableId = zTableAuthority.getTableId();
        if (tableId != null) {
            statement.bindLong(2, tableId.longValue());
        } else {
            statement.bindNull(2);
        }
        String tableName = zTableAuthority.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        Long packageUid = zTableAuthority.getPackageUid();
        if (packageUid != null) {
            statement.bindLong(4, packageUid.longValue());
        } else {
            statement.bindNull(4);
        }
        String packageName = zTableAuthority.getPackageName();
        if (packageName != null) {
            statement.bindString(5, packageName);
        } else {
            statement.bindNull(5);
        }
        Integer authority = zTableAuthority.getAuthority();
        if (authority != null) {
            statement.bindLong(6, (long) authority.intValue());
        } else {
            statement.bindNull(6);
        }
        Boolean supportGroupAuthority = zTableAuthority.getSupportGroupAuthority();
        if (supportGroupAuthority != null) {
            statement.bindLong(7, supportGroupAuthority.booleanValue() ? 1 : 0);
        } else {
            statement.bindNull(7);
        }
        String reserved = zTableAuthority.getReserved();
        if (reserved != null) {
            statement.bindString(8, reserved);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZTableAuthority readObject(Cursor cursor, int i) {
        return new ZTableAuthority(cursor);
    }

    public void setPrimaryKeyValue(ZTableAuthority zTableAuthority, long j) {
        zTableAuthority.setId(Long.valueOf(j));
    }
}
