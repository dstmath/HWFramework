package com.huawei.nb.model.search;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class UniqueSearchConfigHelper extends AEntityHelper<UniqueSearchConfig> {
    private static final UniqueSearchConfigHelper INSTANCE = new UniqueSearchConfigHelper();

    private UniqueSearchConfigHelper() {
    }

    public static UniqueSearchConfigHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, UniqueSearchConfig object) {
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
        String module = object.getModule();
        if (module != null) {
            statement.bindString(4, module);
        } else {
            statement.bindNull(4);
        }
        Integer process = object.getProcess();
        if (process != null) {
            statement.bindLong(5, (long) process.intValue());
        } else {
            statement.bindNull(5);
        }
    }

    public UniqueSearchConfig readObject(Cursor cursor, int offset) {
        return new UniqueSearchConfig(cursor);
    }

    public void setPrimaryKeyValue(UniqueSearchConfig object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, UniqueSearchConfig object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
