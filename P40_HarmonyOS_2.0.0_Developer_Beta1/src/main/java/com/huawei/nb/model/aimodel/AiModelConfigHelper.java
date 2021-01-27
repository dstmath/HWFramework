package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelConfigHelper extends AEntityHelper<AiModelConfig> {
    private static final AiModelConfigHelper INSTANCE = new AiModelConfigHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, AiModelConfig aiModelConfig) {
        return null;
    }

    private AiModelConfigHelper() {
    }

    public static AiModelConfigHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelConfig aiModelConfig) {
        Long id = aiModelConfig.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String data_path = aiModelConfig.getData_path();
        if (data_path != null) {
            statement.bindString(2, data_path);
        } else {
            statement.bindNull(2);
        }
        String key_path = aiModelConfig.getKey_path();
        if (key_path != null) {
            statement.bindString(3, key_path);
        } else {
            statement.bindNull(3);
        }
        Long version = aiModelConfig.getVersion();
        if (version != null) {
            statement.bindLong(4, version.longValue());
        } else {
            statement.bindNull(4);
        }
        Long type = aiModelConfig.getType();
        if (type != null) {
            statement.bindLong(5, type.longValue());
        } else {
            statement.bindNull(5);
        }
        Long status = aiModelConfig.getStatus();
        if (status != null) {
            statement.bindLong(6, status.longValue());
        } else {
            statement.bindNull(6);
        }
        String reserved_1 = aiModelConfig.getReserved_1();
        if (reserved_1 != null) {
            statement.bindString(7, reserved_1);
        } else {
            statement.bindNull(7);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public AiModelConfig readObject(Cursor cursor, int i) {
        return new AiModelConfig(cursor);
    }

    public void setPrimaryKeyValue(AiModelConfig aiModelConfig, long j) {
        aiModelConfig.setId(Long.valueOf(j));
    }
}
