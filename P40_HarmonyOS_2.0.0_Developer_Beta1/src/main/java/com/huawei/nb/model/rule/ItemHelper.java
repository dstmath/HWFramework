package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class ItemHelper extends AEntityHelper<Item> {
    private static final ItemHelper INSTANCE = new ItemHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Item item) {
        return null;
    }

    private ItemHelper() {
    }

    public static ItemHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Item item) {
        Long id = item.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String name = item.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        String type = item.getType();
        if (type != null) {
            statement.bindString(3, type);
        } else {
            statement.bindNull(3);
        }
        Date installTime = item.getInstallTime();
        if (installTime != null) {
            statement.bindLong(4, installTime.getTime());
        } else {
            statement.bindNull(4);
        }
        Long deviceId = item.getDeviceId();
        if (deviceId != null) {
            statement.bindLong(5, deviceId.longValue());
        } else {
            statement.bindNull(5);
        }
        Long parentId = item.getParentId();
        if (parentId != null) {
            statement.bindLong(6, parentId.longValue());
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Item readObject(Cursor cursor, int i) {
        return new Item(cursor);
    }

    public void setPrimaryKeyValue(Item item, long j) {
        item.setId(Long.valueOf(j));
    }
}
