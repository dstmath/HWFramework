package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelVersionControlHelper extends AEntityHelper<AiModelVersionControl> {
    private static final AiModelVersionControlHelper INSTANCE = new AiModelVersionControlHelper();

    private AiModelVersionControlHelper() {
    }

    public static AiModelVersionControlHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelVersionControl object) {
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
        Long current_version = object.getCurrent_version();
        if (current_version != null) {
            statement.bindLong(3, current_version.longValue());
        } else {
            statement.bindNull(3);
        }
    }

    public AiModelVersionControl readObject(Cursor cursor, int offset) {
        return new AiModelVersionControl(cursor);
    }

    public void setPrimaryKeyValue(AiModelVersionControl object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, AiModelVersionControl object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
