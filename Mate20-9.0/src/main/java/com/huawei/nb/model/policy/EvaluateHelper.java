package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class EvaluateHelper extends AEntityHelper<Evaluate> {
    private static final EvaluateHelper INSTANCE = new EvaluateHelper();

    private EvaluateHelper() {
    }

    public static EvaluateHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Evaluate object) {
        Integer stub = object.getStub();
        if (stub != null) {
            statement.bindLong(1, (long) stub.intValue());
        } else {
            statement.bindNull(1);
        }
    }

    public Evaluate readObject(Cursor cursor, int offset) {
        return new Evaluate(cursor);
    }

    public void setPrimaryKeyValue(Evaluate object, long value) {
        object.setStub(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, Evaluate object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
