package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PolicyRuntimeDataHelper extends AEntityHelper<PolicyRuntimeData> {
    private static final PolicyRuntimeDataHelper INSTANCE = new PolicyRuntimeDataHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, PolicyRuntimeData policyRuntimeData) {
        return null;
    }

    private PolicyRuntimeDataHelper() {
    }

    public static PolicyRuntimeDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PolicyRuntimeData policyRuntimeData) {
        Long id = policyRuntimeData.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String serviceName = policyRuntimeData.getServiceName();
        if (serviceName != null) {
            statement.bindString(2, serviceName);
        } else {
            statement.bindNull(2);
        }
        String category = policyRuntimeData.getCategory();
        if (category != null) {
            statement.bindString(3, category);
        } else {
            statement.bindNull(3);
        }
        String name = policyRuntimeData.getName();
        if (name != null) {
            statement.bindString(4, name);
        } else {
            statement.bindNull(4);
        }
        String value = policyRuntimeData.getValue();
        if (value != null) {
            statement.bindString(5, value);
        } else {
            statement.bindNull(5);
        }
        Long timeStamp = policyRuntimeData.getTimeStamp();
        if (timeStamp != null) {
            statement.bindLong(6, timeStamp.longValue());
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public PolicyRuntimeData readObject(Cursor cursor, int i) {
        return new PolicyRuntimeData(cursor);
    }

    public void setPrimaryKeyValue(PolicyRuntimeData policyRuntimeData, long j) {
        policyRuntimeData.setId(Long.valueOf(j));
    }
}
