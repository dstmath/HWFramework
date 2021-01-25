package com.huawei.nb.model.aimodel;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class AiModelStatisticHelper extends AEntityHelper<AiModelStatistic> {
    private static final AiModelStatisticHelper INSTANCE = new AiModelStatisticHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, AiModelStatistic aiModelStatistic) {
        return null;
    }

    private AiModelStatisticHelper() {
    }

    public static AiModelStatisticHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, AiModelStatistic aiModelStatistic) {
        Long id = aiModelStatistic.getId();
        if (id != null) {
            statement.bindLong(1, id.longValue());
        } else {
            statement.bindNull(1);
        }
        Long aimodel_id = aiModelStatistic.getAimodel_id();
        if (aimodel_id != null) {
            statement.bindLong(2, aimodel_id.longValue());
        } else {
            statement.bindNull(2);
        }
        Long last_use_time = aiModelStatistic.getLast_use_time();
        if (last_use_time != null) {
            statement.bindLong(3, last_use_time.longValue());
        } else {
            statement.bindNull(3);
        }
        String last_use_business = aiModelStatistic.getLast_use_business();
        if (last_use_business != null) {
            statement.bindString(4, last_use_business);
        } else {
            statement.bindNull(4);
        }
        Integer use_count = aiModelStatistic.getUse_count();
        if (use_count != null) {
            statement.bindLong(5, (long) use_count.intValue());
        } else {
            statement.bindNull(5);
        }
        String reserved_1 = aiModelStatistic.getReserved_1();
        if (reserved_1 != null) {
            statement.bindString(6, reserved_1);
        } else {
            statement.bindNull(6);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public AiModelStatistic readObject(Cursor cursor, int i) {
        return new AiModelStatistic(cursor);
    }

    public void setPrimaryKeyValue(AiModelStatistic aiModelStatistic, long j) {
        aiModelStatistic.setId(Long.valueOf(j));
    }
}
