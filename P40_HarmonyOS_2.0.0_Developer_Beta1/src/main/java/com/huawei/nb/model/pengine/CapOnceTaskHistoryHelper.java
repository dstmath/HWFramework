package com.huawei.nb.model.pengine;

import android.database.Cursor;
import com.huawei.odmf.database.Statement;
import com.huawei.odmf.model.AEntityHelper;

public class CapOnceTaskHistoryHelper extends AEntityHelper<CapOnceTaskHistory> {
    private static final CapOnceTaskHistoryHelper INSTANCE = new CapOnceTaskHistoryHelper();

    @Override // com.huawei.odmf.model.AEntityHelper
    public int getNumberOfRelationships() {
        return 0;
    }

    public Object getRelationshipObject(String str, CapOnceTaskHistory capOnceTaskHistory) {
        return null;
    }

    private CapOnceTaskHistoryHelper() {
    }

    public static CapOnceTaskHistoryHelper getInstance() {
        return INSTANCE;
    }

    public void bindValue(Statement statement, CapOnceTaskHistory capOnceTaskHistory) {
        Integer hisId = capOnceTaskHistory.getHisId();
        if (hisId != null) {
            statement.bindLong(1, (long) hisId.intValue());
        } else {
            statement.bindNull(1);
        }
        String taskName = capOnceTaskHistory.getTaskName();
        if (taskName != null) {
            statement.bindString(2, taskName);
        } else {
            statement.bindNull(2);
        }
        Integer version = capOnceTaskHistory.getVersion();
        if (version != null) {
            statement.bindLong(3, (long) version.intValue());
        } else {
            statement.bindNull(3);
        }
        String taskType = capOnceTaskHistory.getTaskType();
        if (taskType != null) {
            statement.bindString(4, taskType);
        } else {
            statement.bindNull(4);
        }
        Long executeTime = capOnceTaskHistory.getExecuteTime();
        if (executeTime != null) {
            statement.bindLong(5, executeTime.longValue());
        } else {
            statement.bindNull(5);
        }
    }

    @Override // com.huawei.odmf.model.AEntityHelper
    public CapOnceTaskHistory readObject(Cursor cursor, int i) {
        return new CapOnceTaskHistory(cursor);
    }

    public void setPrimaryKeyValue(CapOnceTaskHistory capOnceTaskHistory, long j) {
        capOnceTaskHistory.setHisId(Integer.valueOf((int) j));
    }
}
