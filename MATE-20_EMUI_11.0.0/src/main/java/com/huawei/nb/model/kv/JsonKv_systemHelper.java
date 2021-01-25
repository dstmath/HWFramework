package com.huawei.nb.model.kv;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class JsonKv_systemHelper extends AEntityHelper<JsonKv_system> {
    private static final JsonKv_systemHelper INSTANCE = new JsonKv_systemHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, JsonKv_system jsonKv_system) {
        return null;
    }

    private JsonKv_systemHelper() {
    }

    public static JsonKv_systemHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, JsonKv_system jsonKv_system) {
        Long id = jsonKv_system.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String primaryKey = jsonKv_system.getPrimaryKey();
        if (primaryKey != null) {
            statement.bindString(2, primaryKey);
        } else {
            statement.bindNull(2);
        }
        String value = jsonKv_system.getValue();
        if (value != null) {
            statement.bindString(3, value);
        } else {
            statement.bindNull(3);
        }
        String version = jsonKv_system.getVersion();
        if (version != null) {
            statement.bindString(4, version);
        } else {
            statement.bindNull(4);
        }
        String owner = jsonKv_system.getOwner();
        if (owner != null) {
            statement.bindString(5, owner);
        } else {
            statement.bindNull(5);
        }
        String tag = jsonKv_system.getTag();
        if (tag != null) {
            statement.bindString(6, tag);
        } else {
            statement.bindNull(6);
        }
        statement.bindLong(7, (long) jsonKv_system.getClone());
        String reserved1 = jsonKv_system.getReserved1();
        if (reserved1 != null) {
            statement.bindString(8, reserved1);
        } else {
            statement.bindNull(8);
        }
        String reserved2 = jsonKv_system.getReserved2();
        if (reserved2 != null) {
            statement.bindString(9, reserved2);
        } else {
            statement.bindNull(9);
        }
        String reserved3 = jsonKv_system.getReserved3();
        if (reserved3 != null) {
            statement.bindString(10, reserved3);
        } else {
            statement.bindNull(10);
        }
        statement.bindLong(11, (long) jsonKv_system.getClearStatus());
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public JsonKv_system readObject(Cursor cursor, int i) {
        return new JsonKv_system(cursor);
    }

    public void setPrimaryKeyValue(JsonKv_system jsonKv_system, long j) {
        jsonKv_system.setId(Long.valueOf(j));
    }
}
