package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelConfigHelper extends AEntityHelper<AiModelConfig> {
    private static final AiModelConfigHelper INSTANCE = new AiModelConfigHelper();

    private AiModelConfigHelper() {
    }

    public static AiModelConfigHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelConfig object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        String data_path = object.getData_path();
        if (data_path != null) {
            statement.bindString(2, data_path);
        } else {
            statement.bindNull(2);
        }
        String key_path = object.getKey_path();
        if (key_path != null) {
            statement.bindString(3, key_path);
        } else {
            statement.bindNull(3);
        }
        Long version = object.getVersion();
        if (version != null) {
            statement.bindLong(4, version.longValue());
        } else {
            statement.bindNull(4);
        }
        Long type = object.getType();
        if (type != null) {
            statement.bindLong(5, type.longValue());
        } else {
            statement.bindNull(5);
        }
        Long status = object.getStatus();
        if (status != null) {
            statement.bindLong(6, status.longValue());
        } else {
            statement.bindNull(6);
        }
        String reserved_1 = object.getReserved_1();
        if (reserved_1 != null) {
            statement.bindString(7, reserved_1);
        } else {
            statement.bindNull(7);
        }
    }

    public AiModelConfig readObject(Cursor cursor, int offset) {
        return new AiModelConfig(cursor);
    }

    public void setPrimaryKeyValue(AiModelConfig object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, AiModelConfig object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
