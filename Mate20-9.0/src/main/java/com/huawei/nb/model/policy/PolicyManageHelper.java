package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PolicyManageHelper extends AEntityHelper<PolicyManage> {
    private static final PolicyManageHelper INSTANCE = new PolicyManageHelper();

    private PolicyManageHelper() {
    }

    public static PolicyManageHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PolicyManage object) {
        String policyName = object.getPolicyName();
        if (policyName != null) {
            statement.bindString(1, policyName);
        } else {
            statement.bindNull(1);
        }
        String serviceName = object.getServiceName();
        if (serviceName != null) {
            statement.bindString(2, serviceName);
        } else {
            statement.bindNull(2);
        }
        String policyFile = object.getPolicyFile();
        if (policyFile != null) {
            statement.bindString(3, policyFile);
        } else {
            statement.bindNull(3);
        }
    }

    public PolicyManage readObject(Cursor cursor, int offset) {
        return new PolicyManage(cursor);
    }

    public void setPrimaryKeyValue(PolicyManage object, long value) {
    }

    public Object getRelationshipObject(String field, PolicyManage object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
