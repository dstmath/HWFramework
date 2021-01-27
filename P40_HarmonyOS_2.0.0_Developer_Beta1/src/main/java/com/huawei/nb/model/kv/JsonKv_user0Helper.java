package com.huawei.nb.model.kv;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class JsonKv_user0Helper extends AEntityHelper<JsonKv_user0> {
    private static final JsonKv_user0Helper INSTANCE = new JsonKv_user0Helper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, JsonKv_user0 jsonKv_user0) {
        return null;
    }

    private JsonKv_user0Helper() {
    }

    public static JsonKv_user0Helper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, JsonKv_user0 jsonKv_user0) {
        Long id = jsonKv_user0.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String primaryKey = jsonKv_user0.getPrimaryKey();
        if (primaryKey != null) {
            statement.bindString(2, primaryKey);
        } else {
            statement.bindNull(2);
        }
        String value = jsonKv_user0.getValue();
        if (value != null) {
            statement.bindString(3, value);
        } else {
            statement.bindNull(3);
        }
        String version = jsonKv_user0.getVersion();
        if (version != null) {
            statement.bindString(4, version);
        } else {
            statement.bindNull(4);
        }
        String owner = jsonKv_user0.getOwner();
        if (owner != null) {
            statement.bindString(5, owner);
        } else {
            statement.bindNull(5);
        }
        String tag = jsonKv_user0.getTag();
        if (tag != null) {
            statement.bindString(6, tag);
        } else {
            statement.bindNull(6);
        }
        statement.bindLong(7, (long) jsonKv_user0.getClone());
        String reserved1 = jsonKv_user0.getReserved1();
        if (reserved1 != null) {
            statement.bindString(8, reserved1);
        } else {
            statement.bindNull(8);
        }
        String reserved2 = jsonKv_user0.getReserved2();
        if (reserved2 != null) {
            statement.bindString(9, reserved2);
        } else {
            statement.bindNull(9);
        }
        String reserved3 = jsonKv_user0.getReserved3();
        if (reserved3 != null) {
            statement.bindString(10, reserved3);
        } else {
            statement.bindNull(10);
        }
        statement.bindLong(11, (long) jsonKv_user0.getClearStatus());
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public JsonKv_user0 readObject(Cursor cursor, int i) {
        return new JsonKv_user0(cursor);
    }

    public void setPrimaryKeyValue(JsonKv_user0 jsonKv_user0, long j) {
        jsonKv_user0.setId(Long.valueOf(j));
    }
}
