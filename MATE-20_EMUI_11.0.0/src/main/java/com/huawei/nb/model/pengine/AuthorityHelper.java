package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AuthorityHelper extends AEntityHelper<Authority> {
    private static final AuthorityHelper INSTANCE = new AuthorityHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Authority authority) {
        return null;
    }

    private AuthorityHelper() {
    }

    public static AuthorityHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Authority authority) {
        Integer id = authority.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        Integer type = authority.getType();
        if (type != null) {
            statement.bindLong(2, (long) type.intValue());
        } else {
            statement.bindNull(2);
        }
        String entity = authority.getEntity();
        if (entity != null) {
            statement.bindString(3, entity);
        } else {
            statement.bindNull(3);
        }
        String right = authority.getRight();
        if (right != null) {
            statement.bindString(4, right);
        } else {
            statement.bindNull(4);
        }
        String column0 = authority.getColumn0();
        if (column0 != null) {
            statement.bindString(5, column0);
        } else {
            statement.bindNull(5);
        }
        String column1 = authority.getColumn1();
        if (column1 != null) {
            statement.bindString(6, column1);
        } else {
            statement.bindNull(6);
        }
        String column2 = authority.getColumn2();
        if (column2 != null) {
            statement.bindString(7, column2);
        } else {
            statement.bindNull(7);
        }
        String column3 = authority.getColumn3();
        if (column3 != null) {
            statement.bindString(8, column3);
        } else {
            statement.bindNull(8);
        }
        String column4 = authority.getColumn4();
        if (column4 != null) {
            statement.bindString(9, column4);
        } else {
            statement.bindNull(9);
        }
        String column5 = authority.getColumn5();
        if (column5 != null) {
            statement.bindString(10, column5);
        } else {
            statement.bindNull(10);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Authority readObject(Cursor cursor, int i) {
        return new Authority(cursor);
    }

    public void setPrimaryKeyValue(Authority authority, long j) {
        authority.setId(Integer.valueOf((int) j));
    }
}
