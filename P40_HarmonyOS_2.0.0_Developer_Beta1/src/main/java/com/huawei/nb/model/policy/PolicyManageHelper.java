package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class PolicyManageHelper extends AEntityHelper<PolicyManage> {
    private static final PolicyManageHelper INSTANCE = new PolicyManageHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, PolicyManage policyManage) {
        return null;
    }

    public void setPrimaryKeyValue(PolicyManage policyManage, long j) {
    }

    private PolicyManageHelper() {
    }

    public static PolicyManageHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, PolicyManage policyManage) {
        String policyName = policyManage.getPolicyName();
        if (policyName != null) {
            statement.bindString(1, policyName);
        } else {
            statement.bindNull(1);
        }
        String serviceName = policyManage.getServiceName();
        if (serviceName != null) {
            statement.bindString(2, serviceName);
        } else {
            statement.bindNull(2);
        }
        String policyFile = policyManage.getPolicyFile();
        if (policyFile != null) {
            statement.bindString(3, policyFile);
        } else {
            statement.bindNull(3);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public PolicyManage readObject(Cursor cursor, int i) {
        return new PolicyManage(cursor);
    }
}
