package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class ParameterHelper extends AEntityHelper<Parameter> {
    private static final ParameterHelper INSTANCE = new ParameterHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Parameter parameter) {
        return null;
    }

    private ParameterHelper() {
    }

    public static ParameterHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Parameter parameter) {
        Integer id = parameter.getId();
        if (id != null) {
            statement.bindLong(1, (long) id.intValue());
        } else {
            statement.bindNull(1);
        }
        String module = parameter.getModule();
        if (module != null) {
            statement.bindString(2, module);
        } else {
            statement.bindNull(2);
        }
        String name = parameter.getName();
        if (name != null) {
            statement.bindString(3, name);
        } else {
            statement.bindNull(3);
        }
        String value = parameter.getValue();
        if (value != null) {
            statement.bindString(4, value);
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Parameter readObject(Cursor cursor, int i) {
        return new Parameter(cursor);
    }

    public void setPrimaryKeyValue(Parameter parameter, long j) {
        parameter.setId(Integer.valueOf((int) j));
    }
}
