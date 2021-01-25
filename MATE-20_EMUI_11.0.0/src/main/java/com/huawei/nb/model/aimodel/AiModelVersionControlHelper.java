package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelVersionControlHelper extends AEntityHelper<AiModelVersionControl> {
    private static final AiModelVersionControlHelper INSTANCE = new AiModelVersionControlHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, AiModelVersionControl aiModelVersionControl) {
        return null;
    }

    private AiModelVersionControlHelper() {
    }

    public static AiModelVersionControlHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelVersionControl aiModelVersionControl) {
        Long id = aiModelVersionControl.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long aimodel_id = aiModelVersionControl.getAimodel_id();
        if (aimodel_id != null) {
            statement.bindLong(2, aimodel_id.longValue());
        } else {
            statement.bindNull(2);
        }
        Long current_version = aiModelVersionControl.getCurrent_version();
        if (current_version != null) {
            statement.bindLong(3, current_version.longValue());
        } else {
            statement.bindNull(3);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public AiModelVersionControl readObject(Cursor cursor, int i) {
        return new AiModelVersionControl(cursor);
    }

    public void setPrimaryKeyValue(AiModelVersionControl aiModelVersionControl, long j) {
        aiModelVersionControl.setId(Long.valueOf(j));
    }
}
