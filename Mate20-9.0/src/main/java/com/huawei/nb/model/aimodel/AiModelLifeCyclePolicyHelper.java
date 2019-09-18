package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelLifeCyclePolicyHelper extends AEntityHelper<AiModelLifeCyclePolicy> {
    private static final AiModelLifeCyclePolicyHelper INSTANCE = new AiModelLifeCyclePolicyHelper();

    private AiModelLifeCyclePolicyHelper() {
    }

    public static AiModelLifeCyclePolicyHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelLifeCyclePolicy object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long aimodel_id = object.getAimodel_id();
        if (aimodel_id != null) {
            statement.bindLong(2, aimodel_id.longValue());
        } else {
            statement.bindNull(2);
        }
        String delete_policy = object.getDelete_policy();
        if (delete_policy != null) {
            statement.bindString(3, delete_policy);
        } else {
            statement.bindNull(3);
        }
        String update_policy = object.getUpdate_policy();
        if (update_policy != null) {
            statement.bindString(4, update_policy);
        } else {
            statement.bindNull(4);
        }
        String reserved_1 = object.getReserved_1();
        if (reserved_1 != null) {
            statement.bindString(5, reserved_1);
        } else {
            statement.bindNull(5);
        }
    }

    public AiModelLifeCyclePolicy readObject(Cursor cursor, int offset) {
        return new AiModelLifeCyclePolicy(cursor);
    }

    public void setPrimaryKeyValue(AiModelLifeCyclePolicy object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, AiModelLifeCyclePolicy object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
