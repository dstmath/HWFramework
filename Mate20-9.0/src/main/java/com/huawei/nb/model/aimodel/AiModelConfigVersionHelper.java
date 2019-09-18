package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelConfigVersionHelper extends AEntityHelper<AiModelConfigVersion> {
    private static final AiModelConfigVersionHelper INSTANCE = new AiModelConfigVersionHelper();

    private AiModelConfigVersionHelper() {
    }

    public static AiModelConfigVersionHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelConfigVersion object) {
        Long id = object.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long type = object.getType();
        if (type != null) {
            statement.bindLong(2, type.longValue());
        } else {
            statement.bindNull(2);
        }
        Long version = object.getVersion();
        if (version != null) {
            statement.bindLong(3, version.longValue());
        } else {
            statement.bindNull(3);
        }
    }

    public AiModelConfigVersion readObject(Cursor cursor, int offset) {
        return new AiModelConfigVersion(cursor);
    }

    public void setPrimaryKeyValue(AiModelConfigVersion object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, AiModelConfigVersion object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
