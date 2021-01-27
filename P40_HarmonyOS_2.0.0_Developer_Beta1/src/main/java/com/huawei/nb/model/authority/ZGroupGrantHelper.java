package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZGroupGrantHelper extends AEntityHelper<ZGroupGrant> {
    private static final ZGroupGrantHelper INSTANCE = new ZGroupGrantHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZGroupGrant zGroupGrant) {
        return null;
    }

    private ZGroupGrantHelper() {
    }

    public static ZGroupGrantHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZGroupGrant zGroupGrant) {
        Long id = zGroupGrant.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String groupName = zGroupGrant.getGroupName();
        if (groupName != null) {
            statement.bindString(2, groupName);
        } else {
            statement.bindNull(2);
        }
        String tableName = zGroupGrant.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        String owner = zGroupGrant.getOwner();
        if (owner != null) {
            statement.bindString(4, owner);
        } else {
            statement.bindNull(4);
        }
        statement.bindLong(5, zGroupGrant.getIsGroupIdentifier() ? 1 : 0);
        String member = zGroupGrant.getMember();
        if (member != null) {
            statement.bindString(6, member);
        } else {
            statement.bindNull(6);
        }
        Integer authority = zGroupGrant.getAuthority();
        if (authority != null) {
            statement.bindLong(7, (long) authority.intValue());
        } else {
            statement.bindNull(7);
        }
        String reserved = zGroupGrant.getReserved();
        if (reserved != null) {
            statement.bindString(8, reserved);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZGroupGrant readObject(Cursor cursor, int i) {
        return new ZGroupGrant(cursor);
    }

    public void setPrimaryKeyValue(ZGroupGrant zGroupGrant, long j) {
        zGroupGrant.setId(Long.valueOf(j));
    }
}
