package com.huawei.nb.model.policy;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class TranslatedPolicyDataHelper extends AEntityHelper<TranslatedPolicyData> {
    private static final TranslatedPolicyDataHelper INSTANCE = new TranslatedPolicyDataHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, TranslatedPolicyData translatedPolicyData) {
        return null;
    }

    private TranslatedPolicyDataHelper() {
    }

    public static TranslatedPolicyDataHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, TranslatedPolicyData translatedPolicyData) {
        Integer policyId = translatedPolicyData.getPolicyId();
        if (policyId != null) {
            statement.bindLong(1, (long) policyId.intValue());
        } else {
            statement.bindNull(1);
        }
        String policyName = translatedPolicyData.getPolicyName();
        if (policyName != null) {
            statement.bindString(2, policyName);
        } else {
            statement.bindNull(2);
        }
        String serviceName = translatedPolicyData.getServiceName();
        if (serviceName != null) {
            statement.bindString(3, serviceName);
        } else {
            statement.bindNull(3);
        }
        String policyFile = translatedPolicyData.getPolicyFile();
        if (policyFile != null) {
            statement.bindString(4, policyFile);
        } else {
            statement.bindNull(4);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public TranslatedPolicyData readObject(Cursor cursor, int i) {
        return new TranslatedPolicyData(cursor);
    }

    public void setPrimaryKeyValue(TranslatedPolicyData translatedPolicyData, long j) {
        translatedPolicyData.setPolicyId(Integer.valueOf((int) j));
    }
}
