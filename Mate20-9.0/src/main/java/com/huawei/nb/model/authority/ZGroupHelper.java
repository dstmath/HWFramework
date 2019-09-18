package com.huawei.nb.model.authority;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ZGroupHelper extends AEntityHelper<ZGroup> {
    private static final ZGroupHelper INSTANCE = new ZGroupHelper();

    private ZGroupHelper() {
    }

    public static ZGroupHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, ZGroup object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String groupName = object.getGroupName();
        if (groupName != null) {
            statement.bindString(2, groupName);
        } else {
            statement.bindNull(2);
        }
        String tableName = object.getTableName();
        if (tableName != null) {
            statement.bindString(3, tableName);
        } else {
            statement.bindNull(3);
        }
        String owner = object.getOwner();
        if (owner != null) {
            statement.bindString(4, owner);
        } else {
            statement.bindNull(4);
        }
        statement.bindLong(5, object.getIsGroupIdentifier() ? 1 : 0);
        String member = object.getMember();
        if (member != null) {
            statement.bindString(6, member);
        } else {
            statement.bindNull(6);
        }
        Integer authority = object.getAuthority();
        if (authority != null) {
            statement.bindLong(7, (long) authority.intValue());
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

    public ZGroup readObject(Cursor cursor, int offset) {
        return new ZGroup(cursor);
    }

    public void setPrimaryKeyValue(ZGroup object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, ZGroup object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
