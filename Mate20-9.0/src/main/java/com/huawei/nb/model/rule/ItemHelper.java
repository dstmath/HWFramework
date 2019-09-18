package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class ItemHelper extends AEntityHelper<Item> {
    private static final ItemHelper INSTANCE = new ItemHelper();

    private ItemHelper() {
    }

    public static ItemHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Item object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        String type = object.getType();
        if (type != null) {
            statement.bindString(3, type);
        } else {
            statement.bindNull(3);
        }
        Date installTime = object.getInstallTime();
        if (installTime != null) {
            statement.bindLong(4, installTime.getTime());
        } else {
            statement.bindNull(4);
        }
        Long deviceId = object.getDeviceId();
        if (deviceId != null) {
            statement.bindLong(5, deviceId.longValue());
        } else {
            statement.bindNull(5);
        }
        Long parentId = object.getParentId();
        if (parentId != null) {
            statement.bindLong(6, parentId.longValue());
        } else {
            statement.bindNull(6);
        }
    }

    public Item readObject(Cursor cursor, int offset) {
        return new Item(cursor);
    }

    public void setPrimaryKeyValue(Item object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, Item object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
