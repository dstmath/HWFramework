package com.huawei.nb.model.meta;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PropertyHelper extends AEntityHelper<Property> {
    private static final PropertyHelper INSTANCE = new PropertyHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Property property) {
        return null;
    }

    private PropertyHelper() {
    }

    public static PropertyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Property property) {
        Integer id = property.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String name = property.getName();
        if (name != null) {
            statement.bindString(2, name);
        } else {
            statement.bindNull(2);
        }
        String value = property.getValue();
        if (value != null) {
            statement.bindString(3, value);
        } else {
            statement.bindNull(3);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Property readObject(Cursor cursor, int i) {
        return new Property(cursor);
    }

    public void setPrimaryKeyValue(Property property, long j) {
        property.setId(Integer.valueOf((int) j));
    }
}
