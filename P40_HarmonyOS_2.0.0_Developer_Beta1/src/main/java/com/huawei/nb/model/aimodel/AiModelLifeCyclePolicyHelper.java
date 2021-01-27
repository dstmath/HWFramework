package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelLifeCyclePolicyHelper extends AEntityHelper<AiModelLifeCyclePolicy> {
    private static final AiModelLifeCyclePolicyHelper INSTANCE = new AiModelLifeCyclePolicyHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, AiModelLifeCyclePolicy aiModelLifeCyclePolicy) {
        return null;
    }

    private AiModelLifeCyclePolicyHelper() {
    }

    public static AiModelLifeCyclePolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelLifeCyclePolicy aiModelLifeCyclePolicy) {
        Long id = aiModelLifeCyclePolicy.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long aimodel_id = aiModelLifeCyclePolicy.getAimodel_id();
        if (aimodel_id != null) {
            statement.bindLong(2, aimodel_id.longValue());
        } else {
            statement.bindNull(2);
        }
        String delete_policy = aiModelLifeCyclePolicy.getDelete_policy();
        if (delete_policy != null) {
            statement.bindString(3, delete_policy);
        } else {
            statement.bindNull(3);
        }
        String update_policy = aiModelLifeCyclePolicy.getUpdate_policy();
        if (update_policy != null) {
            statement.bindString(4, update_policy);
        } else {
            statement.bindNull(4);
        }
        String reserved_1 = aiModelLifeCyclePolicy.getReserved_1();
        if (reserved_1 != null) {
            statement.bindString(5, reserved_1);
        } else {
            statement.bindNull(5);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public AiModelLifeCyclePolicy readObject(Cursor cursor, int i) {
        return new AiModelLifeCyclePolicy(cursor);
    }

    public void setPrimaryKeyValue(AiModelLifeCyclePolicy aiModelLifeCyclePolicy, long j) {
        aiModelLifeCyclePolicy.setId(Long.valueOf(j));
    }
}
