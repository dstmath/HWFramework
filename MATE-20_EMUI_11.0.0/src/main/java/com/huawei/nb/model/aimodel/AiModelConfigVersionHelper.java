package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelConfigVersionHelper extends AEntityHelper<AiModelConfigVersion> {
    private static final AiModelConfigVersionHelper INSTANCE = new AiModelConfigVersionHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, AiModelConfigVersion aiModelConfigVersion) {
        return null;
    }

    private AiModelConfigVersionHelper() {
    }

    public static AiModelConfigVersionHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelConfigVersion aiModelConfigVersion) {
        Long id = aiModelConfigVersion.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long type = aiModelConfigVersion.getType();
        if (type != null) {
            statement.bindLong(2, type.longValue());
        } else {
            statement.bindNull(2);
        }
        Long version = aiModelConfigVersion.getVersion();
        if (version != null) {
            statement.bindLong(3, version.longValue());
        } else {
            statement.bindNull(3);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public AiModelConfigVersion readObject(Cursor cursor, int i) {
        return new AiModelConfigVersion(cursor);
    }

    public void setPrimaryKeyValue(AiModelConfigVersion aiModelConfigVersion, long j) {
        aiModelConfigVersion.setId(Long.valueOf(j));
    }
}
