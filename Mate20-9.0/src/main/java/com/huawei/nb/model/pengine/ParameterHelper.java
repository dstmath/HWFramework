package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ParameterHelper extends AEntityHelper<Parameter> {
    private static final ParameterHelper INSTANCE = new ParameterHelper();

    private ParameterHelper() {
    }

    public static ParameterHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Parameter object) {
        Integer id = object.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String module = object.getModule();
        if (module != null) {
            statement.bindString(2, module);
        } else {
            statement.bindNull(2);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(3, name);
        } else {
            statement.bindNull(3);
        }
        String value = object.getValue();
        if (value != null) {
            statement.bindString(4, value);
        } else {
            statement.bindNull(4);
        }
    }

    public Parameter readObject(Cursor cursor, int offset) {
        return new Parameter(cursor);
    }

    public void setPrimaryKeyValue(Parameter object, long value) {
        object.setId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, Parameter object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
