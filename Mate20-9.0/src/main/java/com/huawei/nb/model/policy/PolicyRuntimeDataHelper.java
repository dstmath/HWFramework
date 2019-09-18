package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PolicyRuntimeDataHelper extends AEntityHelper<PolicyRuntimeData> {
    private static final PolicyRuntimeDataHelper INSTANCE = new PolicyRuntimeDataHelper();

    private PolicyRuntimeDataHelper() {
    }

    public static PolicyRuntimeDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PolicyRuntimeData object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String serviceName = object.getServiceName();
        if (serviceName != null) {
            statement.bindString(2, serviceName);
        } else {
            statement.bindNull(2);
        }
        String category = object.getCategory();
        if (category != null) {
            statement.bindString(3, category);
        } else {
            statement.bindNull(3);
        }
        String name = object.getName();
        if (name != null) {
            statement.bindString(4, name);
        } else {
            statement.bindNull(4);
        }
        String value = object.getValue();
        if (value != null) {
            statement.bindString(5, value);
        } else {
            statement.bindNull(5);
        }
        Long timeStamp = object.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(6, timeStamp.longValue());
        } else {
            statement.bindNull(6);
        }
    }

    public PolicyRuntimeData readObject(Cursor cursor, int offset) {
        return new PolicyRuntimeData(cursor);
    }

    public void setPrimaryKeyValue(PolicyRuntimeData object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, PolicyRuntimeData object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
