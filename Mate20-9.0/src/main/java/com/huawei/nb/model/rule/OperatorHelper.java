package com.huawei.nb.model.rule;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class OperatorHelper extends AEntityHelper<Operator> {
    private static final OperatorHelper INSTANCE = new OperatorHelper();

    private OperatorHelper() {
    }

    public static OperatorHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Operator object) {
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
        Long parentId = object.getParentId();
        if (parentId != null) {
            statement.bindLong(4, parentId.longValue());
        } else {
            statement.bindNull(4);
        }
    }

    public Operator readObject(Cursor cursor, int offset) {
        return new Operator(cursor);
    }

    public void setPrimaryKeyValue(Operator object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, Operator object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
