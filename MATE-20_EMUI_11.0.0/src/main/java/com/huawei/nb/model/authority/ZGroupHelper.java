package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZGroupHelper extends AEntityHelper<ZGroup> {
    private static final ZGroupHelper INSTANCE = new ZGroupHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, ZGroup zGroup) {
        return null;
    }

    private ZGroupHelper() {
    }

    public static ZGroupHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZGroup zGroup) {
        Long id = zGroup.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String groupName = zGroup.getGroupName();
        if (groupName != null) {
            statement.bindString(2, groupName);
        } else {
            statement.bindNull(2);
        }
        String tableName = zGroup.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        String owner = zGroup.getOwner();
        if (owner != null) {
            statement.bindString(4, owner);
        } else {
            statement.bindNull(4);
        }
        statement.bindLong(5, zGroup.getIsGroupIdentifier() ? 1 : 0);
        String member = zGroup.getMember();
        if (member != null) {
            statement.bindString(6, member);
        } else {
            statement.bindNull(6);
        }
        Integer authority = zGroup.getAuthority();
        if (authority != null) {
            statement.bindLong(7, (long) authority.intValue());
        } else {
            statement.bindNull(7);
        }
        String reserved = zGroup.getReserved();
        if (reserved != null) {
            statement.bindString(8, reserved);
        } else {
            statement.bindNull(8);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public ZGroup readObject(Cursor cursor, int i) {
        return new ZGroup(cursor);
    }

    public void setPrimaryKeyValue(ZGroup zGroup, long j) {
        zGroup.setId(Long.valueOf(j));
    }
}
