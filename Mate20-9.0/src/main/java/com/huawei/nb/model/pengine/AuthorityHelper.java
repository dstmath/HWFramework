package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AuthorityHelper extends AEntityHelper<Authority> {
    private static final AuthorityHelper INSTANCE = new AuthorityHelper();

    private AuthorityHelper() {
    }

    public static AuthorityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Authority object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer type = object.getType();
        if (type != null) {
            statement.bindLong(2, (long) type.intValue());
        } else {
            statement.bindNull(2);
        }
        String entity = object.getEntity();
        if (entity != null) {
            statement.bindString(3, entity);
        } else {
            statement.bindNull(3);
        }
        String right = object.getRight();
        if (right != null) {
            statement.bindString(4, right);
        } else {
            statement.bindNull(4);
        }
        String column0 = object.getColumn0();
        if (column0 != null) {
            statement.bindString(5, column0);
        } else {
            statement.bindNull(5);
        }
        String column1 = object.getColumn1();
        if (column1 != null) {
            statement.bindString(6, column1);
        } else {
            statement.bindNull(6);
        }
        String column2 = object.getColumn2();
        if (column2 != null) {
            statement.bindString(7, column2);
        } else {
            statement.bindNull(7);
        }
        String column3 = object.getColumn3();
        if (column3 != null) {
            statement.bindString(8, column3);
        } else {
            statement.bindNull(8);
        }
        String column4 = object.getColumn4();
        if (column4 != null) {
            statement.bindString(9, column4);
        } else {
            statement.bindNull(9);
        }
        String column5 = object.getColumn5();
        if (column5 != null) {
            statement.bindString(10, column5);
        } else {
            statement.bindNull(10);
        }
    }

    public Authority readObject(Cursor cursor, int offset) {
        return new Authority(cursor);
    }

    public void setPrimaryKeyValue(Authority object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, Authority object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
