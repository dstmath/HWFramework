package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class EvaluateHelper extends AEntityHelper<Evaluate> {
    private static final EvaluateHelper INSTANCE = new EvaluateHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, Evaluate evaluate) {
        return null;
    }

    private EvaluateHelper() {
    }

    public static EvaluateHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, Evaluate evaluate) {
        Integer stub = evaluate.getStub();
        if (stub != null) {
            statement.bindLong(1, (long) stub.intValue());
        } else {
            statement.bindNull(1);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public Evaluate readObject(Cursor cursor, int i) {
        return new Evaluate(cursor);
    }

    public void setPrimaryKeyValue(Evaluate evaluate, long j) {
        evaluate.setStub(Integer.valueOf((int) j));
    }
}
