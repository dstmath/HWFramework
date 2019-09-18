package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class BusinessHelper extends AEntityHelper<Business> {
    private static final BusinessHelper INSTANCE = new BusinessHelper();

    private BusinessHelper() {
    }

    public static BusinessHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Business object) {
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
        Integer businessType = object.getBusinessType();
        if (businessType != null) {
            statement.bindLong(3, (long) businessType.intValue());
        } else {
            statement.bindNull(3);
        }
        Integer level = object.getLevel();
        if (level != null) {
            statement.bindLong(4, (long) level.intValue());
        } else {
            statement.bindNull(4);
        }
        String description = object.getDescription();
        if (description != null) {
            statement.bindString(5, description);
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

    public Business readObject(Cursor cursor, int offset) {
        return new Business(cursor);
    }

    public void setPrimaryKeyValue(Business object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, Business object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
