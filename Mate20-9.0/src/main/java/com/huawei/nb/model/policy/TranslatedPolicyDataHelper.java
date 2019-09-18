package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class TranslatedPolicyDataHelper extends AEntityHelper<TranslatedPolicyData> {
    private static final TranslatedPolicyDataHelper INSTANCE = new TranslatedPolicyDataHelper();

    private TranslatedPolicyDataHelper() {
    }

    public static TranslatedPolicyDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, TranslatedPolicyData object) {
        Integer policyId = object.getPolicyId();
        if (policyId != null) {
            statement.bindLong(1, (long) policyId.intValue());
        } else {
            statement.bindNull(1);
        }
        String policyName = object.getPolicyName();
        if (policyName != null) {
            statement.bindString(2, policyName);
        } else {
            statement.bindNull(2);
        }
        String serviceName = object.getServiceName();
        if (serviceName != null) {
            statement.bindString(3, serviceName);
        } else {
            statement.bindNull(3);
        }
        String policyFile = object.getPolicyFile();
        if (policyFile != null) {
            statement.bindString(4, policyFile);
        } else {
            statement.bindNull(4);
        }
    }

    public TranslatedPolicyData readObject(Cursor cursor, int offset) {
        return new TranslatedPolicyData(cursor);
    }

    public void setPrimaryKeyValue(TranslatedPolicyData object, long value) {
        object.setPolicyId(Integer.valueOf((int) value));
    }

    public Object getRelationshipObject(String field, TranslatedPolicyData object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
