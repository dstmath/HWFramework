package com.huawei.nb.model.kv;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class JsonKv_user0Helper extends AEntityHelper<JsonKv_user0> {
    private static final JsonKv_user0Helper INSTANCE = new JsonKv_user0Helper();

    private JsonKv_user0Helper() {
    }

    public static JsonKv_user0Helper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, JsonKv_user0 object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String primaryKey = object.getPrimaryKey();
        if (primaryKey != null) {
            statement.bindString(2, primaryKey);
        } else {
            statement.bindNull(2);
        }
        String value = object.getValue();
        if (value != null) {
            statement.bindString(3, value);
        } else {
            statement.bindNull(3);
        }
        String version = object.getVersion();
        if (version != null) {
            statement.bindString(4, version);
        } else {
            statement.bindNull(4);
        }
        String owner = object.getOwner();
        if (owner != null) {
            statement.bindString(5, owner);
        } else {
            statement.bindNull(5);
        }
        String tag = object.getTag();
        if (tag != null) {
            statement.bindString(6, tag);
        } else {
            statement.bindNull(6);
        }
        statement.bindLong(7, (long) object.getClone());
        String reserved1 = object.getReserved1();
        if (reserved1 != null) {
            statement.bindString(8, reserved1);
        } else {
            statement.bindNull(8);
        }
        String reserved2 = object.getReserved2();
        if (reserved2 != null) {
            statement.bindString(9, reserved2);
        } else {
            statement.bindNull(9);
        }
        String reserved3 = object.getReserved3();
        if (reserved3 != null) {
            statement.bindString(10, reserved3);
        } else {
            statement.bindNull(10);
        }
        statement.bindLong(11, (long) object.getClearStatus());
    }

    public JsonKv_user0 readObject(Cursor cursor, int offset) {
        return new JsonKv_user0(cursor);
    }

    public void setPrimaryKeyValue(JsonKv_user0 object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, JsonKv_user0 object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
