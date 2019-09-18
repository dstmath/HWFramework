package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelTobeDeletedHelper extends AEntityHelper<AiModelTobeDeleted> {
    private static final AiModelTobeDeletedHelper INSTANCE = new AiModelTobeDeletedHelper();

    private AiModelTobeDeletedHelper() {
    }

    public static AiModelTobeDeletedHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelTobeDeleted object) {
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
        String name = object.getName();
        if (name != null) {
            statement.bindString(3, name);
        } else {
            statement.bindNull(3);
        }
        String file_path = object.getFile_path();
        if (file_path != null) {
            statement.bindString(4, file_path);
        } else {
            statement.bindNull(4);
        }
        Long time_expired = object.getTime_expired();
        if (time_expired != null) {
            statement.bindLong(5, time_expired.longValue());
        } else {
            statement.bindNull(5);
        }
    }

    public AiModelTobeDeleted readObject(Cursor cursor, int offset) {
        return new AiModelTobeDeleted(cursor);
    }

    public void setPrimaryKeyValue(AiModelTobeDeleted object, long value) {
        object.setId(Long.valueOf(value));
    }

    public Object getRelationshipObject(String field, AiModelTobeDeleted object) {
        return null;
    }

    public int getNumberOfRelationships() {
        return 0;
    }
}
