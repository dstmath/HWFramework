package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PropertyHelper extends AEntityHelper<Property> {
    private static final PropertyHelper INSTANCE = new PropertyHelper();

    private PropertyHelper() {
    }

    public static PropertyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Property object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        String value = object.getValue();
        if (value != null) {
            statement.bindString(3, value);
        } else {
            statement.bindNull(3);
        }
    }

    public Property readObject(Cursor cursor, int offset) {
        return new Property(cursor);
    }

    public void setPrimaryKeyValue(Property object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, Property object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
